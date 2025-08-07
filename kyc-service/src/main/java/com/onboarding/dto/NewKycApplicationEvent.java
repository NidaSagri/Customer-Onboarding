package com.onboarding.dto;

public class NewKycApplicationEvent {
    private Long applicationId;
    private String applicantName;
    private String applicantEmail;

    public NewKycApplicationEvent() {}

    public NewKycApplicationEvent(Long applicationId, String applicantName, String applicantEmail) {
        this.applicationId = applicationId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
    }

    // Getters and Setters...
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }
}