package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.model.User;
import com.onboarding.service.KycApplicationService;
import com.onboarding.service.KycService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final KycApplicationService kycApplicationService;
    private final KycService kycService;
    private final AccountClient accountClient; // Inject the Feign client

    public AdminController(KycApplicationService kycAppService, KycService kycService, AccountClient accountClient) {
        this.kycApplicationService = kycAppService;
        this.kycService = kycService;
        this.accountClient = accountClient;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<KycApplication> applicationPage = kycApplicationService.findAllApplications(pageable);
        model.addAttribute("applications", applicationPage);

        model.addAttribute("totalApplications", kycApplicationService.countTotalApplications());
        model.addAttribute("pendingKyc", kycApplicationService.countPendingApplications());
        model.addAttribute("verifiedCustomers", kycApplicationService.countVerifiedApplications());

        return "admin/dashboard";
    }

    @GetMapping("/application/{id}")
    public String applicationDetails(@PathVariable Long id, Model model) {
        KycApplication app = kycApplicationService.findApplicationById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid application Id:" + id));
        model.addAttribute("KycDetails", app);

        // If the application is verified, try to fetch the account details
        if (app.getKycStatus() == KycStatus.VERIFIED) {
            try {
                // First, get the user to find the customerId
                Optional<User> userOpt = accountClient.getUserByUsername(app.getUsername());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    // Then, use the customerId to get the account
                    AccountDTO account = accountClient.getAccountByCustomerId(user.getCustomerId());
                    model.addAttribute("account", account);
                }
            } catch (Exception e) {
                // It's okay if an account isn't found, we just won't display it.
                // This could happen if the account service is down or if there's a delay.
                model.addAttribute("accountError", "Could not retrieve account details.");
            }
        }

        return "admin/application-details";
    }

    @PostMapping("/application/{id}/process")
    public String processKycApplication(@PathVariable("id") Long applicationId,
                                        @RequestParam boolean approved,
                                        @RequestParam(required = false) String rejectionReason,
                                        RedirectAttributes redirectAttributes) {
        try {
            kycService.processKycApplication(applicationId, approved, rejectionReason);
            String status = approved ? "approved" : "rejected";
            redirectAttributes.addFlashAttribute("message", "Application " + applicationId + " has been " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing application: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}