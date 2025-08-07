package com.onboarding.dto;

import java.math.BigDecimal;

/**
 * A Data Transfer Object for passing account information between services.
 */
public class AccountDTO {

    private String accountNumber;
    private String accountStatus;
    private BigDecimal balance;

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}