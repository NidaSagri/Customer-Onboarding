package com.onboarding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDTO {

    private Long customerId;
    private String accountNumber;
    private String accountType;
    private String accountStatus;
    private BigDecimal balance;
    
    // --- New fields to match the entity ---
    private String branchName;
    private String ifscCode;
    private String micrCode;
    private LocalDateTime dateOfAccountOpening;
    private String modeOfOperation;
    private boolean nomineeRegistered;
    private String nomineeName;
    private boolean netBankingEnabled;
    private boolean debitCardIssued;
    private String debitCardLast4Digits;
    private boolean chequeBookIssued;
    private Integer chequeBookLeaves;

    // --- Manual Getters and Setters ---

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
    public String getMicrCode() { return micrCode; }
    public void setMicrCode(String micrCode) { this.micrCode = micrCode; }
    public LocalDateTime getDateOfAccountOpening() { return dateOfAccountOpening; }
    public void setDateOfAccountOpening(LocalDateTime dateOfAccountOpening) { this.dateOfAccountOpening = dateOfAccountOpening; }
    public String getModeOfOperation() { return modeOfOperation; }
    public void setModeOfOperation(String modeOfOperation) { this.modeOfOperation = modeOfOperation; }
    public boolean isNomineeRegistered() { return nomineeRegistered; }
    public void setNomineeRegistered(boolean nomineeRegistered) { this.nomineeRegistered = nomineeRegistered; }
    public String getNomineeName() { return nomineeName; }
    public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }
    public boolean isNetBankingEnabled() { return netBankingEnabled; }
    public void setNetBankingEnabled(boolean netBankingEnabled) { this.netBankingEnabled = netBankingEnabled; }
    public boolean isDebitCardIssued() { return debitCardIssued; }
    public void setDebitCardIssued(boolean debitCardIssued) { this.debitCardIssued = debitCardIssued; }
    public String getDebitCardLast4Digits() { return debitCardLast4Digits; }
    public void setDebitCardLast4Digits(String debitCardLast4Digits) { this.debitCardLast4Digits = debitCardLast4Digits; }
    public boolean isChequeBookIssued() { return chequeBookIssued; }
    public void setChequeBookIssued(boolean chequeBookIssued) { this.chequeBookIssued = chequeBookIssued; }
    public Integer getChequeBookLeaves() { return chequeBookLeaves; }
    public void setChequeBookLeaves(Integer chequeBookLeaves) { this.chequeBookLeaves = chequeBookLeaves; }
}