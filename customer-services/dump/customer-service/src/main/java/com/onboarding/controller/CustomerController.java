package com.onboarding.controller;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.model.KycApplication; // <-- IMPORT KycApplication
import com.onboarding.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Handles API requests to submit a new KYC application.
     * This is the first step in the new, two-stage onboarding process.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        LOGGER.info("API call received for /api/customers/register to submit a new KYC application.");
        try {
            // --- THE FIX: Call the new service method ---
            KycApplication newApplication = customerService.registerKycApplication(request);
            
            // --- THE FIX: Update the response message ---
            String responseBody = "KYC Application submitted successfully with Application ID: " + newApplication.getId();
            LOGGER.info("KYC Application submitted successfully with ID: {}", newApplication.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception e) {
            LOGGER.error("KYC Application submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("CustomerController is active!");
    }
}