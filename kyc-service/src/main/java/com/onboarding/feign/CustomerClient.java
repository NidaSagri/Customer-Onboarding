package com.onboarding.feign;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.dto.KycApplicationDataDTO; // Ensure this DTO exists in kyc-service
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerClient {

    /**
     * Calls the internal API on customer-service to create a new, fully approved customer.
     * This is the final step in the orchestration after an admin approves the KYC.
     */
    @PostMapping("/api/internal/customers/create-from-kyc")
    CustomerDTO createApprovedCustomer(@RequestBody KycApplicationDataDTO kycData);
}