// src/main/java/com/onboarding/dto/TransactionRequestDTO.java
package com.onboarding.dto;

import java.math.BigDecimal;

public class TransactionRequestDTO {
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;

    // Getters and Setters
    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }
    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}