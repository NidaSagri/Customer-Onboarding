package com.onboarding.service;

import com.onboarding.model.Account;
import com.onboarding.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Creates an INACTIVE account with all customer-selected preferences and bank-configured details.
     * It now expects a richer map of data.
     */
    @Transactional
    public Account createInactiveAccount(Map<String, Object> creationData) {
        Long customerId = Long.parseLong(creationData.get("customerId").toString());
        LOGGER.info("Attempting to create inactive account for customer ID: {}", customerId);

        accountRepository.findByCustomerId(customerId).ifPresent(acc -> {
            throw new RuntimeException("Customer already has an account.");
        });

        Account account = new Account();
        
        // --- Core Details ---
        account.setCustomerId(customerId);
        account.setAccountType((String) creationData.get("accountType"));
        account.setAccountStatus("INACTIVE");
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setCreatedAt(LocalDateTime.now());
        
        // --- Bank-Specific Details (Hardcoded for this example) ---
        account.setBranchName("HDFC Bank, Vashi Branch");
        account.setIfscCode("HDFC0000123");
        account.setMicrCode("400240123");
        account.setModeOfOperation("Single"); // Default mode

        // --- Details from Customer Registration ---
        account.setNomineeRegistered((Boolean) creationData.getOrDefault("nomineeRegistered", false));
        account.setNomineeName((String) creationData.get("nomineeName")); // Will be null if not registered
        account.setNetBankingEnabled((Boolean) creationData.getOrDefault("netBankingEnabled", false));
        account.setDebitCardIssued((Boolean) creationData.getOrDefault("debitCardIssued", false));
        account.setChequeBookIssued((Boolean) creationData.getOrDefault("chequeBookIssued", false));

        Account savedAccount = accountRepository.save(account);
        LOGGER.info("Successfully created INACTIVE account {} for customer ID: {}", savedAccount.getAccountNumber(), customerId);
        return savedAccount;
    }
    
    /**
     * Activates an account and populates final details like opening date and card/cheque info.
     */
 // In AccountService.java

    @Transactional
    public Account activateAccount(Long customerId) {
        LOGGER.info("Attempting to activate account for customer ID: {}", customerId);
        Account account = accountRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Account not found for customer ID: " + customerId));
        
        account.setAccountStatus("ACTIVE");
        account.setDateOfAccountOpening(LocalDateTime.now());

        // *** THE FIX IS HERE: Changed from is...() to get...() ***

        // If a debit card was requested, generate the last 4 digits now.
        if (Boolean.TRUE.equals(account.getDebitCardIssued())) {
            account.setDebitCardLast4Digits(generateLastFourDigits());
        }

        // If a cheque book was requested, set the number of leaves.
        if (Boolean.TRUE.equals(account.getChequeBookIssued())) {
            account.setChequeBookLeaves(25); // Default number of leaves
        }

        Account updatedAccount = accountRepository.save(account);
        LOGGER.info("Successfully ACTIVATED account {} for customer ID: {}", updatedAccount.getAccountNumber(), customerId);
        return updatedAccount;
    }
    
    // ... The rest of the service remains the same ...

    private String generateAccountNumber() {
        // Generates a random 12-digit number string
        long number = ThreadLocalRandom.current().nextLong(100_000_000_000L, 1_000_000_000_000L);
        return String.valueOf(number);
    }
    
    private String generateLastFourDigits() {
        // Generates a random 4-digit number string
        return String.format("%04d", new Random().nextInt(10000));
    }
}