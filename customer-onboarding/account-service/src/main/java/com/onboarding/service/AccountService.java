package com.onboarding.service;

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

    // The CustomerClient dependency is now removed.
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createInactiveAccount(Long customerId, String accountType) {
        // The business logic validation (checking KYC status) is now solely the
        // responsibility of the kyc-service before it even calls this method.
        // This service now trusts that any request to create an account is valid.

        LOGGER.info("Attempting to create inactive account for customer ID: {}", customerId);
        accountRepository.findByCustomerId(customerId).ifPresent(acc -> {
            throw new RuntimeException("Customer already has an account.");
        });

        Account account = new Account();
        account.setCustomerId(customerId);
        account.setAccountType(accountType);
        account.setAccountStatus("INACTIVE");
        account.setAccountNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        account.setBalance(BigDecimal.ZERO);
        
        Account savedAccount = accountRepository.save(account);
        LOGGER.info("Successfully created INACTIVE account {} for customer ID: {}", savedAccount.getAccountNumber(), customerId);
        return savedAccount;
    }
    
    @Transactional
    public Account activateAccount(Long customerId) {
        LOGGER.info("Attempting to activate account for customer ID: {}", customerId);
        Account account = accountRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Account not found for customer ID: " + customerId));
        
        account.setAccountStatus("ACTIVE");
        Account updatedAccount = accountRepository.save(account);
        LOGGER.info("Successfully ACTIVATED account {} for customer ID: {}", updatedAccount.getAccountNumber(), customerId);
        return updatedAccount;
    }
}