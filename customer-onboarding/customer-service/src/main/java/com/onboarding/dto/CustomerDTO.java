package com.onboarding.dto;

/**
 * A Data Transfer Object used for communication between microservices.
 * It carries a subset of a Customer's data. In this case, it's used by the
 * account-service to verify a customer's KYC status before creating an account.
 */
public class CustomerDTO {

    private Long id;
    private String kycStatus; // e.g., "PENDING", "VERIFIED", "REJECTED"

    // A no-argument constructor is required for JSON deserialization
    public CustomerDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }
}