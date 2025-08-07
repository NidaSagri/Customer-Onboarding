package com.onboarding.dto;

import java.time.LocalDate;

/**
 * DTO to receive a complete, approved KYC application from the kyc-service.
 * This DTO now contains all fields necessary to create a full Customer record.
 */
public class KycApplicationDataDTO {

    // I've renamed this from kycApplicationId to 'id' to match the KycApplication entity
    private Long id; 

    // --- Personal Information ---
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String maritalStatus;
    private String fathersName;
    private String nationality;
    private String profession;
    private String address;

    // --- Contact & Identity Information ---
    private String email;
    private String phone;
    private String pan;
    private String aadhaar;

    // --- Account & Login Choices ---
    private String username;
    private String password; // This password comes pre-encrypted from the kyc-service

    // --- Getters and Setters for all fields ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getFathersName() { return fathersName; }
    public void setFathersName(String fathersName) { this.fathersName = fathersName; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }
    public String getAadhaar() { return aadhaar; }
    public void setAadhaar(String aadhaar) { this.aadhaar = aadhaar; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}