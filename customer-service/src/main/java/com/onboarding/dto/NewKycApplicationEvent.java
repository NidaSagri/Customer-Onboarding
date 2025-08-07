package com.onboarding.dto;

import java.io.Serializable;

// This class represents an event sent when a new KYC application is submitted.
// It needs to exist in both the Producer and Consumer services.
public class NewKycApplicationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long kycApplicationId;
    private String applicantName;
    private String applicantEmail;

    // A no-argument constructor is needed for deserialization
    public NewKycApplicationEvent() {
    }

    public NewKycApplicationEvent(Long kycApplicationId, String applicantName, String applicantEmail) {
        this.kycApplicationId = kycApplicationId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
    }

    // Getters and Setters
    public Long getKycApplicationId() {
        return kycApplicationId;
    }

    public void setKycApplicationId(Long kycApplicationId) {
        this.kycApplicationId = kycApplicationId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    @Override
    public String toString() {
        return "NewKycApplicationEvent{" +
                "kycApplicationId=" + kycApplicationId +
                ", applicantName='" + applicantName + '\'' +
                ", applicantEmail='" + applicantEmail + '\'' +
                '}';
    }
}