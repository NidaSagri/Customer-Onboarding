package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.KycApplication;
import com.onboarding.service.KycApplicationService;
import com.onboarding.service.KycService;
// We still need Customer for the edit form model attribute
import com.onboarding.model.Customer; 
import com.onboarding.service.CustomerQueryService; // <-- IMPORT NEW SERVICE
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final KycApplicationService kycApplicationService;
    private final KycService kycService;
    private final CustomerQueryService customerQueryService; // <-- INJECT NEW SERVICE
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
        model.addAttribute("totalApplications", kycApplicationService.countTotalApplications());
        model.addAttribute("pendingKyc", kycApplicationService.countPendingApplications());
        
        // This stat should count final, approved customers
        model.addAttribute("verifiedCustomers", customerQueryService.countTotalCustomers());
        
        return "admin/dashboard";
    }

    @GetMapping("/application/{id}")
    public String applicationDetails(@PathVariable Long id, Model model) {
        KycApplication application = kycApplicationService.findApplicationById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid application Id:" + id));
        model.addAttribute("application", application);
        return "admin/application-details";
    }
    
    @PostMapping("/application/{id}/verify")
    public String verifyKycApplication(@PathVariable("id") Long applicationId, @RequestParam boolean approved, @RequestParam(required = false) String rejectionReason, RedirectAttributes redirectAttributes) {
        try {
            kycService.processKycApplication(applicationId, approved, rejectionReason);
            String status = approved ? "approved and promoted to customer" : "rejected";
            redirectAttributes.addFlashAttribute("message", "Application " + applicationId + " has been " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing application: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // The edit/delete functions would operate on KycApplication now, not Customer.
    // For simplicity, we can assume admins cannot edit/delete submitted applications, only process them.
}