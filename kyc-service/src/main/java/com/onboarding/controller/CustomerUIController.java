package com.onboarding.controller;

import com.onboarding.model.KycApplication;
import com.onboarding.repository.KycApplicationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer") // All URLs in this controller will start with /customer
public class CustomerUIController {

    private final KycApplicationRepository kycApplicationRepository;

    public CustomerUIController(KycApplicationRepository kycApplicationRepository) {
        this.kycApplicationRepository = kycApplicationRepository;
    }

    @GetMapping("/dashboard")
    public String showCustomerDashboard(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login"; // Should not happen due to security config, but safe
        }
        
        // The username from the security context is used to find the application
        String username = authentication.getName();
        
        KycApplication application = kycApplicationRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Could not find application for logged in user: " + username));
            
        model.addAttribute("application", application);
        
        // This is where we will add logic for account details IF the status is VERIFIED
        // For now, we will just show the application status.
        if ("VERIFIED".equals(application.getKycStatus().name())) {
            // In a later step, we would make a Feign call here to get account details
            // model.addAttribute("account", accountClient.getAccountByCustomerId(...));
        }

        return "customer/dashboard";
    }
}