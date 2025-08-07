package com.onboarding.feign;

import com.onboarding.dto.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountClient {

	 @PostMapping("/api/internal/accounts/create-inactive")
	    AccountDTO createInactiveAccount(@RequestBody Map<String, Object> creationData);

	    @PostMapping("/api/internal/accounts/customer/{customerId}/activate")
	    AccountDTO activateAccount(@PathVariable("customerId") Long customerId);

    @GetMapping("/api/internal/accounts/customer/{customerId}")
    AccountDTO getAccountByCustomerId(@PathVariable("customerId") Long customerId);

    @GetMapping("/api/internal/accounts/by-customer-ids")
    List<AccountDTO> getAccountsByCustomerIds(@RequestParam("customerIds") List<Long> customerIds);
    
    @GetMapping("/api/internal/accounts/by-kyc-id/{kycId}")
    AccountDTO getAccountByKycApplicationId(@PathVariable("kycId") Long kycId);

    @GetMapping("/api/internal/accounts/created-between")
    List<AccountDTO> getAccountsCreatedBetween(
        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);
}