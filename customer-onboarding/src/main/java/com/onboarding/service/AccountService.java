package com.onboarding.service;

import com.onboarding.model.*;
import com.onboarding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
    @Autowired private AccountRepository accountRepository;
    @Autowired private CustomerRepository customerRepository;

 // Change the method signature to accept the deposit
    public Account createAccount(Long customerId, String accountType, BigDecimal initialDeposit) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (customer.getKycStatus() != KycStatus.VERIFIED) {
            throw new RuntimeException("Cannot create account. KYC is not verified.");
        }

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountType(accountType);
        account.setAccountStatus("ACTIVE");
        // Generate a mock account number
        account.setAccountNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        
        //account.setBalance(BigDecimal.ZERO); // Always initialize the balance to 0
        // --- THE FIX ---
        // Set the balance from the request. Add a null check for safety.
        account.setBalance(initialDeposit != null ? initialDeposit : BigDecimal.ZERO);
        
        return accountRepository.save(account);
    }
    
    public Optional<Account> findAccountByCustomer(Customer customer) {
        return accountRepository.findByCustomer(customer);
    }
}