package com.onboarding.controller;

import com.onboarding.dto.KycApplicationDataDTO; // Ensure this DTO exists in customer-service
import com.onboarding.model.Customer;
import com.onboarding.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/customers")
public class InternalApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalApiController.class);
    private final CustomerService customerService;

    public InternalApiController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * This endpoint is called by the kyc-service ONLY AFTER an admin has approved an application.
     * It receives the full application data and creates the permanent Customer and User records.
     */
    @PostMapping("/create-from-kyc")
    public ResponseEntity<Customer> createApprovedCustomer(@RequestBody KycApplicationDataDTO kycData) {
        LOGGER.info("Received request from kyc-service to create approved customer for application ID: {}", kycData.getId());
        try {
            Customer newCustomer = customerService.createApprovedCustomer(kycData);
            LOGGER.info("Successfully created approved customer with ID: {}", newCustomer.getId());
            return ResponseEntity.ok(newCustomer);
        } catch (Exception e) {
            LOGGER.error("Failed to create approved customer for app ID {}. Error: {}", kycData.getId(), e.getMessage(), e);
            // Return a meaningful error response
            return ResponseEntity.badRequest().body(null);
        }
    }
}