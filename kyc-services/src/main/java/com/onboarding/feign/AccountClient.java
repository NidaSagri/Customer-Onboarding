package com.onboarding.feign;

import com.onboarding.dto.AccountDTO;
import com.onboarding.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

// The name "ACCOUNT-SERVICE" must match the spring.application.name in the account-service's properties
@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountClient {

    /**
     * Calls an internal API on the account-service to get account details for a given customer ID.
     */
    @GetMapping("/api/internal/accounts/customer/{customerId}")
    AccountDTO getAccountByCustomerId(@PathVariable("customerId") Long customerId);

    /**
     * Fetches the User object which contains the customerId needed to get account details.
     */
    @GetMapping("/api/internal/users/by-username/{username}")
    Optional<User> getUserByUsername(@PathVariable("username") String username);
}