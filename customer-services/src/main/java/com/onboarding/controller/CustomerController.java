package com.onboarding.controller;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.model.Customer;
import com.onboarding.service.CustomerQueryService;
import com.onboarding.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

	private final CustomerService customerService;
    private final CustomerQueryService customerQueryService;

    public CustomerController(CustomerService customerService, CustomerQueryService customerQueryService) {
        this.customerService = customerService;
        this.customerQueryService = customerQueryService;
    }

    /**
     * Internal endpoint for the kyc-service to call upon approving an application.
     * It creates the permanent customer record.
     */
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDto) {
        try {
            Customer newCustomer = customerService.createCustomer(customerDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(customerQueryService.mapToDto(newCustomer));
        } catch (IllegalStateException e) {
            // This happens if a customer with the same PAN/Aadhaar already exists.
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Internal endpoint for any other microservice to retrieve a verified customer's details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        return customerQueryService.findCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}