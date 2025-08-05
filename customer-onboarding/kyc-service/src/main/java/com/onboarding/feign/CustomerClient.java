package com.onboarding.feign;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.dto.KycApplicationDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerClient {

    @PostMapping("/api/internal/customers/create-from-kyc")
    CustomerDTO createApprovedCustomer(@RequestBody KycApplicationDataDTO kycData);
    @GetMapping("/api/internal/customers/by-kyc-id/{kycId}")
    CustomerDTO getCustomerByKycApplicationId(@PathVariable("kycId") Long kycId);
}