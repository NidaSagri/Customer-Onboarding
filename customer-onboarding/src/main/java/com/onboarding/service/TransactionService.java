package com.onboarding.service;

import com.onboarding.model.Account;
import com.onboarding.model.Transaction;
import com.onboarding.model.TransactionStatus;
import com.onboarding.repository.AccountRepository;
import com.onboarding.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction performPayment(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new RuntimeException("Sender and receiver accounts cannot be the same.");
        }
        
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
            .orElseThrow(() -> new RuntimeException("Sender account not found."));
            
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
            .orElseThrow(() -> new RuntimeException("Receiver account not found."));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transaction amount must be positive.");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds.");
        }
        
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction();
        transaction.setFromAccountNumber(fromAccountNumber);
        transaction.setToAccountNumber(toAccountNumber);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Retrieves the transaction history for a specific account.
     * This method is used by both Admin and Customer UIs.
     */
    public List<Transaction> getTransactionHistory(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return List.of(); // Return empty list if account number is invalid
        }
        // Use the corrected repository method name
        return transactionRepository.findTransactionsByAccountNumber(accountNumber);
    }
}