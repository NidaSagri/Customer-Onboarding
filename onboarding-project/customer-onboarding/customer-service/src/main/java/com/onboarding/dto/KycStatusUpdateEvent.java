package com.onboarding.dto;

public class KycStatusUpdateEvent {
    private String customerName;
    private String customerEmail;
    private String kycStatus; // "VERIFIED" or "REJECTED"
    private String rejectionReason; // <-- NEW FIELD
    
    // New fields for the approval email
    private String accountNumber;
    private String accountType;
    private String ifscCode;

    public KycStatusUpdateEvent() {}

    // Getters and Setters for all fields...
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
}