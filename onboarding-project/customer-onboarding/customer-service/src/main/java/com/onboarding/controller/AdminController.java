package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.KycApplication; // <-- WORKS WITH KycApplication
import com.onboarding.service.KycApplicationService; // <-- USES A NEW DEDICATED SERVICE
import com.onboarding.service.KycService;
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

    private final KycApplicationService kycApplicationService; // <-- NEW SERVICE
    private final KycService kycService;

    // Simplified constructor for the new workflow
    public AdminController(KycApplicationService kycApplicationService, KycService kycService) {
        this.kycApplicationService = kycApplicationService;
        this.kycService = kycService;
    }
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Fetches a page of KYC applications instead of Customers
        Page<KycApplication> applicationPage = kycApplicationService.findAllApplications(pageable);

        model.addAttribute("applications", applicationPage); // Pass applications to the view
        model.addAttribute("totalApplications", kycApplicationService.countTotalApplications());
        model.addAttribute("pendingKyc", kycApplicationService.countPendingApplications());
        // You would add other stats if needed
        
        return "admin/dashboard"; // This will render the admin dashboard
    }

    // This page is now for viewing an APPLICATION, not a Customer
    @GetMapping("/application/{id}")
    public String applicationDetails(@PathVariable Long id, Model model) {
        KycApplication application = kycApplicationService.findApplicationById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid application Id:" + id));
        model.addAttribute("application", application);
        
        // No account details to show here yet, as the account hasn't been created.
        
        return "admin/application-details"; // We will need a new HTML page for this
    }
    
    // The verify method now takes an applicationId
    @PostMapping("/application/{id}/verify")
    public String verifyKyc(
            @PathVariable("id") Long applicationId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason,
            RedirectAttributes redirectAttributes) {
        try {
            // --- THE FIX: Call the correct service method ---
            kycService.processKycApplication(applicationId, approved, rejectionReason);
            
            String status = approved ? "approved and promoted to customer" : "rejected";
            redirectAttributes.addFlashAttribute("message", "Application " + applicationId + " has been " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing application: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // We can add delete/edit for KycApplication if needed, but they are less common.
    // The old Customer-related endpoints are no longer relevant for this controller's primary role.
}