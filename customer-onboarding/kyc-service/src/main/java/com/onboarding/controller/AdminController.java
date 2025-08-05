package com.onboarding.controller;

import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.service.KycApplicationService;
import com.onboarding.service.KycProcessingService;
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
    private final KycProcessingService kycProcessingService;

    // The AccountClient is no longer needed in this controller
    public AdminController(KycApplicationService kycAppService, KycProcessingService kycProcessingService) {
        this.kycApplicationService = kycAppService;
        this.kycProcessingService = kycProcessingService;
    }
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        // This method is now correct and doesn't need changes.
        Pageable pageable = PageRequest.of(page, size);
        Page<KycApplication> applicationPage = kycApplicationService.findAllApplications(pageable);
        model.addAttribute("applications", applicationPage);
        model.addAttribute("totalApplications", applicationPage.getTotalElements());
        model.addAttribute("pendingKyc", kycApplicationService.countByKycStatus(KycStatus.PENDING));
        model.addAttribute("verifiedAccounts", kycApplicationService.countByKycStatus(KycStatus.VERIFIED));
        return "admin/dashboard";
    }

    @GetMapping("/kyc-application/{id}")
    public String applicationDetails(@PathVariable Long id, Model model) {
        // This logic is now simplified and robust.
        // It fetches the application and passes it to the view. That is its only job.
        KycApplication application = kycApplicationService.findApplicationById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid application Id:" + id));
        
        model.addAttribute("application", application);
        
        return "admin/application-details";
    }
    
    @PostMapping("/kyc-application/{id}/process")
    public String processKyc(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason,
            RedirectAttributes redirectAttributes) {
        try {
            kycProcessingService.processKycApplication(id, approved, rejectionReason);
            String status = approved ? "approved" : "rejected";
            redirectAttributes.addFlashAttribute("message", "KYC Application " + id + " has been " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing application: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}