package com.onboarding.controller;

import com.onboarding.dto.KycApplicationDataDTO;
import com.onboarding.model.Customer;
import com.onboarding.repository.CustomerRepository;
import com.onboarding.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/customers")
public class InternalApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalApiController.class);
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public InternalApiController(CustomerService customerService,CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository=customerRepository;
    }

    @PostMapping("/create-from-kyc")
    public ResponseEntity<Customer> createApprovedCustomer(@RequestBody KycApplicationDataDTO kycData) {
        LOGGER.info("Received request from kyc-service to create approved customer for application ID: {}", kycData.getKycApplicationId());
        try {
            Customer newCustomer = customerService.createApprovedCustomer(kycData);
            LOGGER.info("Successfully created approved customer with ID: {}", newCustomer.getId());
            return ResponseEntity.ok(newCustomer);
        } catch (Exception e) {
            LOGGER.error("Failed to create approved customer. Error: {}", e.getMessage());
            // In a real system, you might return a more specific error status
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/by-kyc-id/{kycId}")
    public ResponseEntity<Customer> getCustomerByKycId(@PathVariable Long kycId) {
        return customerRepository.findByKycApplicationId(kycId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}