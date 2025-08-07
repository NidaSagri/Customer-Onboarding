package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.KycApplication;
import com.onboarding.service.KycApplicationService;
import com.onboarding.service.KycService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import com.onboarding.service.CustomerQueryService;
import com.onboarding.model.Customer;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final KycApplicationService kycApplicationService;
    private final KycService kycService;
    private final CustomerQueryService customerQueryService;
    private final AccountClient accountClient;

    public AdminController(KycApplicationService kycApplicationService, KycService kycService, CustomerQueryService customerQueryService, AccountClient accountClient) {
        this.kycApplicationService = kycApplicationService;
        this.kycService = kycService;
        this.customerQueryService = customerQueryService;
        this.accountClient = accountClient;
    }
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<KycApplication> applicationPage = kycApplicationService.findAllApplications(pageable);
        model.addAttribute("applications", applicationPage);

        // This part needs to be refactored to fetch accounts for VERIFIED applications only if needed
        // For now, focusing on the main list.
        
        model.addAttribute("totalApplications", kycApplicationService.countTotalApplications());
        model.addAttribute("pendingKyc", kycApplicationService.countPendingApplications());
        model.addAttribute("verifiedCustomers", customerQueryService.countTotalCustomers());
        return "admin/dashboard";
    }

    @GetMapping("/application/{id}")
    public String applicationDetails(@PathVariable Long id, Model model) {
        KycApplication application = kycApplicationService.findApplicationById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid application Id:" + id));
        model.addAttribute("application", application);
        
        // --- THE FIX: Try to find a promoted customer and their account ---
        // This is important for viewing applications that have already been processed.
        customerQueryService.findCustomerByEmail(application.getEmail()).ifPresent(customer -> {
            try {
                AccountDTO account = accountClient.getAccountByCustomerId(customer.getId());
                model.addAttribute("account", account);
            } catch (Exception e) {
                // No account found for the approved customer, which is a possible state.
            }
        });
        
        return "admin/application-details";
    }
    
    @PostMapping("/application/{id}/verify")
    public String verifyKycApplication(@PathVariable("id") Long applicationId, @RequestParam boolean approved, @RequestParam(required = false) String rejectionReason, RedirectAttributes redirectAttributes) {
        try {
            kycService.processKycApplication(applicationId, approved, rejectionReason);
            String status = approved ? "approved and promoted" : "rejected";
            redirectAttributes.addFlashAttribute("message", "Application " + applicationId + " has been " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing application: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}