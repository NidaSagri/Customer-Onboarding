package com.onboarding.dto;

import java.math.BigDecimal;

/**
 * A Data Transfer Object used for communication between microservices.
 * It carries a subset of an Account's data.
 */
public class AccountDTO {

    private Long customerId; // <-- THE NEW, REQUIRED FIELD
    private String accountNumber;
    private String accountType;
    private String accountStatus;
    private BigDecimal balance;

    public AccountDTO() {
    }

    // Getters and Setters for all fields
    public Long getCustomerId() { // <-- THE NEW GETTER
        return customerId;
    }

    public void setCustomerId(Long customerId) { // <-- THE NEW SETTER
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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