package com.onboarding.controller;

import com.onboarding.dto.AccountCreationRequest;
import com.onboarding.dto.AccountDTO; // <-- IMPORT
import com.onboarding.model.Account;
import com.onboarding.repository.AccountRepository;
import com.onboarding.service.AccountService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // <-- IMPORT

@RestController
@RequestMapping("/api/internal/accounts")
public class InternalApiController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public InternalApiController(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }
    
    // Helper method to convert Entity to DTO
    private AccountDTO convertToDto(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setCustomerId(account.getCustomerId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountType(account.getAccountType());
        dto.setAccountStatus(account.getAccountStatus());
        dto.setBalance(account.getBalance());
        return dto;
    }

    @PostMapping("/create-and-activate")
    public ResponseEntity<?> createAndActivateAccount(@RequestBody AccountCreationRequest request) {
        try {
            Account account = accountService.createActiveAccount(request);
            // We can return the full Account object or a DTO.
            // For inter-service, returning the entity is fine if it doesn't have complex relationships.
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create active account: " + e.getMessage());
        }
    }
    

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<AccountDTO> getAccountByCustomerId(@PathVariable Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .map(this::convertToDto) // <-- Convert to DTO before sending
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-customer-ids")
    public ResponseEntity<List<AccountDTO>> getAccountsByCustomerIds(@RequestParam("customerIds") List<Long> customerIds) {
        List<Account> accounts = accountRepository.findByCustomerIdIn(customerIds);
        // Convert the list of entities to a list of DTOs
        List<AccountDTO> accountDtos = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }
    
    @GetMapping("/created-between")
    public ResponseEntity<List<AccountDTO>> getAccountsCreatedBetween(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
                
        List<Account> accounts = accountRepository.findByCreatedAtBetween(start, end);
        List<AccountDTO> accountDtos = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountDtos);
    }
}