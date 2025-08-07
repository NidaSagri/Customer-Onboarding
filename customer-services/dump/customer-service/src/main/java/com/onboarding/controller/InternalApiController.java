package com.onboarding.controller;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.service.CustomerQueryService; // <-- USE THE CORRECT SERVICE
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller provides internal-only API endpoints for other microservices to call.
 * It queries the final "golden record" CUSTOMERS table.
 */
@RestController
@RequestMapping("/api/internal")
public class InternalApiController {

    private final CustomerQueryService customerQueryService; // <-- INJECT THE QUERY SERVICE

    public InternalApiController(CustomerQueryService customerQueryService) {
        this.customerQueryService = customerQueryService;
    }

    /**
     * This is the specific endpoint that the account-service's FeignClient will call.
     * It finds an approved customer and returns their data as a CustomerDTO.
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        // Use the new query service to find the customer
        return customerQueryService.findCustomerById(id)
                .map(customer -> {
                    // Manually convert the Customer Entity to a CustomerDTO
                    CustomerDTO dto = new CustomerDTO();
                    dto.setId(customer.getId());
                    dto.setKycStatus(customer.getKycStatus().name());
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}