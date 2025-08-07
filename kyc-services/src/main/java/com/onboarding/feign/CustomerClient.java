package com.onboarding.feign;

import com.onboarding.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// The name "CUSTOMER-SERVICE" must match the spring.application.name of the other microservice
@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerClient {

    /**
     * Calls the internal API of the customer-service to create a new permanent customer record.
     * @param customerDto The DTO containing all the verified customer data.
     * @return A DTO of the newly created customer, including its new ID.
     */
    @PostMapping("/api/internal/customers")
    ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDto);
}