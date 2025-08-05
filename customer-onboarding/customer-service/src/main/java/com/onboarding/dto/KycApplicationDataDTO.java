package com.onboarding.dto;

import java.time.LocalDate;

/**
 * DTO to receive approved KYC data from the kyc-service.
 */
public class KycApplicationDataDTO {
    private Long kycApplicationId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dob;
    private String address;
    private String pan;
    private String aadhaar;
    private String username;
    private String password; // This password comes pre-encrypted from the kyc-service

    // Getters and Setters
    public Long getKycApplicationId() { return kycApplicationId; }
    public void setKycApplicationId(Long kycApplicationId) { this.kycApplicationId = kycApplicationId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }
    public String getAadhaar() { return aadhaar; }
    public void setAadhaar(String aadhaar) { this.aadhaar = aadhaar; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}