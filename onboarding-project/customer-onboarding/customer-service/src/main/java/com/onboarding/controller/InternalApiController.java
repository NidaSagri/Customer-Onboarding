package com.onboarding.controller;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller provides internal-only API endpoints for other microservices to call.
 * It should not be exposed to the public directly. The API Gateway will not route
 * public traffic to /api/internal.
 */
@RestController
@RequestMapping("/api/internal")
public class InternalApiController {

    private final CustomerService customerService;

    public InternalApiController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * This is the specific endpoint that the account-service's FeignClient will call.
     * It finds a customer and returns their data as a CustomerDTO.
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        return customerService.findCustomerById(id)
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