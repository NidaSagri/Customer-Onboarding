package com.onboarding.service;

import com.onboarding.dto.AccountCreationRequest;
import com.onboarding.model.Account;
import com.onboarding.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Creates a new, fully detailed, and ACTIVE bank account.
     * This is called by the customer-service only after KYC has been approved.
     */
    @Transactional
    public Account createActiveAccount(AccountCreationRequest request) {
        LOGGER.info("Creating a new ACTIVE account for customer ID: {}", request.getCustomerId());
        
        accountRepository.findByCustomerId(request.getCustomerId()).ifPresent(acc -> {
            throw new RuntimeException("Error: Customer already has an account.");
        });

        Account account = new Account();
        account.setCustomerId(request.getCustomerId());
        account.setAccountType(request.getAccountType());
        
        // Populate all new fields
        account.setAccountNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        account.setAccountStatus("ACTIVE"); // Set to ACTIVE immediately
        account.setBranchName("OFSS Bank, Mumbai Digital Branch"); // Mock data
        account.setIfscCode("OFSS0001234"); // Mock data
        account.setMicrCode("400240123"); // Mock data
        account.setBalance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO);
        account.setDateOfAccountOpening(LocalDate.now());
        account.setModeOfOperation("SINGLE"); // Default to single operation
        // Other fields like nominee can be set via an "update profile" feature later

        return accountRepository.save(account);
    }
}