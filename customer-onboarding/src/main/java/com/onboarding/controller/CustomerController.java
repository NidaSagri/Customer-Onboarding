package com.onboarding.controller;

import com.onboarding.model.Customer;
import com.onboarding.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers") // Base path for all customer-related APIs
public class CustomerController {

    private final CustomerService customerService;

    // Constructor Injection is best practice
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Handles API requests to register a new customer.
     * This is called by your JavaScript frontend or Postman.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody Customer customer) {
        Customer registeredCustomer = customerService.registerCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body("Customer registered successfully with ID: " + registeredCustomer.getId());
    }
}