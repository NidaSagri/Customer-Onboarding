package com.onboarding.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ACCOUNTS")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "CUSTOMER_ID", unique = true) // One customer has one account in this design
    private Long customerId;

    // --- Core Account Details ---
    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountType; // "SAVINGS" or "CURRENT"

    @Column(nullable = false)
    private String accountStatus; // "INACTIVE" or "ACTIVE"

    // --- Branch and Identification ---
    @Column(nullable = false)
    private String branchName;

    @Column(nullable = false)
    private String ifscCode;

    @Column(nullable = false)
    private String micrCode;

    // --- Financials ---
    @Column(nullable = false)
    private BigDecimal balance;

    // --- Dates and Configuration ---
    private LocalDate dateOfAccountOpening;

    @Column(nullable = false)
    private String modeOfOperation; // e.g., "SINGLE"

    private boolean nomineeRegistered = false;

    private String nomineeName; // Nullable if nomineeRegistered is false

    private boolean netBankingEnabled = true;

    private boolean chequeBookIssued = false;

    private LocalDateTime createdAt = LocalDateTime.now();


    // Getters and Setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
    public String getMicrCode() { return micrCode; }
    public void setMicrCode(String micrCode) { this.micrCode = micrCode; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public LocalDate getDateOfAccountOpening() { return dateOfAccountOpening; }
    public void setDateOfAccountOpening(LocalDate dateOfAccountOpening) { this.dateOfAccountOpening = dateOfAccountOpening; }
    public String getModeOfOperation() { return modeOfOperation; }
    public void setModeOfOperation(String modeOfOperation) { this.modeOfOperation = modeOfOperation; }
    public boolean isNomineeRegistered() { return nomineeRegistered; }
    public void setNomineeRegistered(boolean nomineeRegistered) { this.nomineeRegistered = nomineeRegistered; }
    public String getNomineeName() { return nomineeName; }
    public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }
    public boolean isNetBankingEnabled() { return netBankingEnabled; }
    public void setNetBankingEnabled(boolean netBankingEnabled) { this.netBankingEnabled = netBankingEnabled; }
    public boolean isChequeBookIssued() { return chequeBookIssued; }
    public void setChequeBookIssued(boolean chequeBookIssued) { this.chequeBookIssued = chequeBookIssued; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}