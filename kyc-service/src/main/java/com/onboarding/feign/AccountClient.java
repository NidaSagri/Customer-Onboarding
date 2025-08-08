package com.onboarding.feign;

import com.onboarding.dto.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountClient {

    @PostMapping("/api/internal/accounts/create-inactive")
    AccountDTO createInactiveAccount(@RequestBody Map<String, Object> creationData);

    @PostMapping("/api/internal/accounts/customer/{customerId}/activate")
    AccountDTO activateAccount(@PathVariable("customerId") Long customerId);
    
    // *** ENSURE THIS METHOD EXISTS ***
    @GetMapping("/api/internal/accounts/customer/{customerId}")
    AccountDTO getAccountByCustomerId(@PathVariable("customerId") Long customerId);
}