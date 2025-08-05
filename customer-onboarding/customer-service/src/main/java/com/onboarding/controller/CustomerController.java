package com.onboarding.controller;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.model.Customer;
import com.onboarding.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers") // This maps the whole class to /api/customers
public class CustomerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register") // This maps the method to POST /api/customers/register
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        LOGGER.info("API call received for /api/customers/register");
        try {
            Customer registeredCustomer = customerService.registerCustomer(request);
            String responseBody = "Customer registered successfully with ID: " + registeredCustomer.getId();
            LOGGER.info("Registration successful for customer ID: {}", registeredCustomer.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception e) {
            LOGGER.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // --- A SIMPLE DEBUGGING ENDPOINT ---
    // We can use this to test if the controller is being scanned at all.
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("CustomerController is active!");
    }
}