package com.onboarding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// This DTO should be identical across all services that use it.
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private Long customerId;
    
    // --- THIS IS THE FIELD THAT IS MISSING ---
    private String ifscCode;

    // --- GETTERS ---
    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public String getStatus() { return status; }
    public BigDecimal getBalance() { return balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getCustomerId() { return customerId; }
    
    // --- THIS IS THE GETTER METHOD THAT IS MISSING ---
    public String getIfscCode() { return ifscCode; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setStatus(String status) { this.status = status; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
}