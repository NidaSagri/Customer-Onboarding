package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.model.Account;
import com.onboarding.repository.AccountRepository;
import com.onboarding.service.AccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/internal/accounts")
public class InternalApiController {
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public InternalApiController(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }
    
    private AccountDTO convertToDto(Account account) {
        // This helper method ensures a consistent DTO format
        AccountDTO dto = new AccountDTO();
        dto.setCustomerId(account.getCustomerId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountType(account.getAccountType());
        dto.setAccountStatus(account.getAccountStatus());
        dto.setBalance(account.getBalance());
        return dto;
    }

    @PostMapping("/create-inactive")
    public ResponseEntity<AccountDTO> createInactiveAccount(@RequestBody Map<String, Object> creationData) {
        Long customerId = Long.parseLong(creationData.get("customerId").toString());
        String accountType = creationData.get("accountType").toString();
        Account account = accountService.createInactiveAccount(customerId, accountType);
        return ResponseEntity.ok(convertToDto(account));
    }

    @PostMapping("/customer/{customerId}/activate")
    public ResponseEntity<AccountDTO> activateAccount(@PathVariable Long customerId) {
        Account account = accountService.activateAccount(customerId);
        return ResponseEntity.ok(convertToDto(account));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<AccountDTO> getAccountByCustomerId(@PathVariable Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-customer-ids")
    public ResponseEntity<List<AccountDTO>> getAccountsByCustomerIds(@RequestParam("customerIds") List<Long> customerIds) {
        return ResponseEntity.ok(accountRepository.findByCustomerIdIn(customerIds).stream()
                .map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/created-between")
    public ResponseEntity<List<AccountDTO>> getAccountsCreatedBetween(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(accountRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::convertToDto).collect(Collectors.toList()));
    }
}