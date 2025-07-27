package com.onboarding.service;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.feign.CustomerClient;
import com.onboarding.model.Account;
import com.onboarding.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CustomerClient customerClient; // The Feign client

    public AccountService(AccountRepository accountRepository, CustomerClient customerClient) {
        this.accountRepository = accountRepository;
        this.customerClient = customerClient;
    }

    /**
     * Creates a new account with an INACTIVE status. This is called by customer-service
     * during the initial customer registration.
     */
    @Transactional
    public Account createInactiveAccount(Long customerId, String accountType) {
        LOGGER.info("Attempting to create inactive account for customer ID: {}", customerId);
        accountRepository.findByCustomerId(customerId).ifPresent(acc -> {
            throw new RuntimeException("Customer already has an account.");
        });

        Account account = new Account();
        account.setCustomerId(customerId);
        account.setAccountType(accountType);
        account.setAccountStatus("INACTIVE"); // Start as inactive
        account.setAccountNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        account.setBalance(BigDecimal.ZERO); // Initial balance is always zero
        
        Account savedAccount = accountRepository.save(account);
        LOGGER.info("Successfully created INACTIVE account {} for customer ID: {}", savedAccount.getAccountNumber(), customerId);
        return savedAccount;
    }
    
    /**
     * Activates an existing account. This is called by customer-service
     * after a customer's KYC has been successfully verified.
     */
    @Transactional
    public Account activateAccount(Long customerId) {
        LOGGER.info("Attempting to activate account for customer ID: {}", customerId);
        Account account = accountRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Account not found for customer ID: " + customerId));
        
        if ("ACTIVE".equals(account.getAccountStatus())) {
             LOGGER.warn("Account {} for customer ID {} is already active.", account.getAccountNumber(), customerId);
             return account;
        }
        
        account.setAccountStatus("ACTIVE");
        Account updatedAccount = accountRepository.save(account);
        LOGGER.info("Successfully ACTIVATED account {} for customer ID: {}", updatedAccount.getAccountNumber(), customerId);
        return updatedAccount;
    }
}