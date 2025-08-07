package com.onboarding.dto;

import java.time.LocalDate;

public class CustomerRegistrationRequest {

    // Using a dedicated nested class is cleaner than depending on the main model
    private CustomerData customer = new CustomerData();
    private String username;
    private String password;
    private String accountType;

    // --- NESTED STATIC CLASS TO HOLD ALL CUSTOMER DATA ---
    public static class CustomerData {
        private String fullName;
        private String fatherName;
        private String motherName; // <-- THE MISSING FIELD
        private String gender;
        private String maritalStatus;
        private String nationality;
        private String profession;
        private String email;
        private String phone;
        private LocalDate dob;
        private String address;
        private String pan;
        private String aadhaar;
        private String aadhaarPhotoBase64;
        private String panPhotoBase64;
        private String passportPhotoBase64;

        // Getters and Setters for all fields within CustomerData
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getFatherName() { return fatherName; }
        public void setFatherName(String fatherName) { this.fatherName = fatherName; }
        public String getMotherName() { return motherName; }
        public void setMotherName(String motherName) { this.motherName = motherName; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getMaritalStatus() { return maritalStatus; }
        public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }
        public String getProfession() { return profession; }
        public void setProfession(String profession) { this.profession = profession; }
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
        public String getAadhaarPhotoBase64() { return aadhaarPhotoBase64; }
        public void setAadhaarPhotoBase64(String aadhaarPhotoBase64) { this.aadhaarPhotoBase64 = aadhaarPhotoBase64; }
        public String getPanPhotoBase64() { return panPhotoBase64; }
        public void setPanPhotoBase64(String panPhotoBase64) { this.panPhotoBase64 = panPhotoBase64; }
        public String getPassportPhotoBase64() { return passportPhotoBase64; }
        public void setPassportPhotoBase64(String passportPhotoBase64) { this.passportPhotoBase64 = passportPhotoBase64; }
    }

    // Getters and Setters for the main class fields
    public CustomerData getCustomer() { return customer; }
    public void setCustomer(CustomerData customer) { this.customer = customer; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}