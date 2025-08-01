package com.onboarding.dto;

// This DTO represents the data we get from the customer-service
public class CustomerDTO {
    private Long id;
    private String kycStatus; // We only need the KYC status for our business logic

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