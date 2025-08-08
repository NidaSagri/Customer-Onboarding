package com.onboarding.feign;

import com.onboarding.dto.CustomerCreationResponseDTO;
import com.onboarding.dto.KycApplicationDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerClient {

    /**
     * Calls the internal API on customer-service to create a new, fully approved customer.
     * This now expects to receive the lightweight CustomerCreationResponseDTO upon success.
     */
    @PostMapping("/api/internal/customers/create-from-kyc")
    CustomerCreationResponseDTO createApprovedCustomer(@RequestBody KycApplicationDataDTO kycData);
}