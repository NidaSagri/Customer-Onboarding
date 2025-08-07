package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.KycApplication;
import com.onboarding.repository.KycApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer")
public class CustomerUIController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerUIController.class);

    private final KycApplicationRepository kycApplicationRepository;
    private final AccountClient accountClient;

    public CustomerUIController(KycApplicationRepository kycApplicationRepository, AccountClient accountClient) {
        this.kycApplicationRepository = kycApplicationRepository;
        this.accountClient = accountClient;
    }

    @GetMapping("/dashboard")
    public String showCustomerDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        KycApplication application = kycApplicationRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Could not find application for user: " + username));
            
        model.addAttribute("kycApp", application);
        
        // Fetch account details ONLY if KYC is verified and the customerId has been set
        if (application.getCustomerId() != null && "VERIFIED".equals(application.getKycStatus().name())) {
            try {
                LOGGER.info("KYC is verified for user {}. Fetching account details for customer ID: {}", username, application.getCustomerId());
                
                // *** THE FIX: Use the customerId from the application to fetch the account ***
                AccountDTO account = accountClient.getAccountByCustomerId(application.getCustomerId());
                model.addAttribute("account", account);
                
                LOGGER.info("Successfully fetched account details for user {}", username);
            } catch (Exception e) {
                LOGGER.error("Error fetching account details for user {}: {}", username, e.getMessage());
                model.addAttribute("accountError", "Could not retrieve your approved account details at this time.");
            }
        }

        return "customer/dashboard";
    }
}