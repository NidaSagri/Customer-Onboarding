package com.onboarding.controller;

import com.onboarding.dto.AccountCreationRequest;
import com.onboarding.model.Account;
import com.onboarding.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired private AccountService accountService;

    // 2.4 Account Creation
    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody AccountCreationRequest request) {
        try {
            Account account = accountService.createAccount(
                request.getCustomerId(),
                request.getAccountType(),
                request.getInitialDeposit() // <-- Pass the new value here
            );
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}