package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.repository.KycApplicationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer")
public class CustomerUIController {

    private final KycApplicationRepository kycApplicationRepository;
    private final AccountClient accountClient;

    public CustomerUIController(KycApplicationRepository kycRepo, AccountClient accClient) {
        this.kycApplicationRepository = kycRepo;
        this.accountClient = accClient;
    }

    @GetMapping("/dashboard")
    public String customerDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        
        // Find the approved application to get the user's details
        KycApplication application = kycApplicationRepository
                .findByUsernameOrEmailAndKycStatus(username, KycStatus.VERIFIED)
                .orElseThrow(() -> new RuntimeException("Could not find approved application for user: " + username));
        
        model.addAttribute("application", application);

        // Call account-service to get the associated account details
        try {
            AccountDTO account = accountClient.getAccountByCustomerId(application.getId());
            model.addAttribute("account", account);
        } catch (Exception e) {
            // This case should ideally not happen for an approved user, but it's good practice.
        }
        
        return "customer/dashboard";
    }
}