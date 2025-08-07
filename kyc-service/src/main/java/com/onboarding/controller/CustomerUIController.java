package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient; // Make sure this is imported
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
    // *** STEP 1: Declare the Feign Client as a field ***
    private final AccountClient accountClient;

    /**
     * *** STEP 2: Add AccountClient to the constructor ***
     * Spring will see this and inject the Feign client bean.
     */
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
        
        // Fetch account details if KYC is verified
        if (application.getKycStatus() != null && "VERIFIED".equals(application.getKycStatus().name())) {
            try {
                LOGGER.info("KYC is verified for user {}. Fetching account details for KYC application ID: {}", username, application.getId());
                // *** STEP 3: Call the method on the injected instance, not the interface ***
                AccountDTO account = accountClient.getAccountByKycApplicationId(application.getId());
                model.addAttribute("account", account);
                LOGGER.info("Successfully fetched account details for user {}", username);
            } catch (Exception e) {
                LOGGER.error("Error fetching account details for user {}: {}", username, e.getMessage());
                // Handle case where account might not be found (e.g., Feign client error)
                model.addAttribute("accountError", "Could not retrieve your approved account details at this time.");
            }
        }

        return "customer/dashboard";
    }
}