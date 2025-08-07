package com.onboarding.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CustomerRegistrationRequest {

    @Valid
    @NotNull
    private CustomerData customer = new CustomerData();

    @NotBlank(message = "Username is mandatory")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    // --- THIS IS THE FIX ---
    @NotBlank(message = "Account type is mandatory")
    private String accountType; // e.g., "SAVINGS", "CURRENT"

    // (The nested CustomerData class remains the same)
    public static class CustomerData {
        @NotBlank(message = "Full name is mandatory")
        private String fullName;

        @NotBlank(message = "Father's name is mandatory")
        private String fatherName;

        @NotBlank(message = "Mother's name is mandatory")
        private String motherName;

        @NotBlank(message = "Gender is mandatory")
        private String gender;
        
        @NotBlank(message = "Marital status is mandatory")
        private String maritalStatus;

        @NotBlank(message = "Nationality is mandatory")
        private String nationality;

        @NotBlank(message = "Profession is mandatory")
        private String profession;

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Phone number is mandatory")
        @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
        private String phone;

        @NotNull(message = "Date of birth is mandatory")
        @Past(message = "Date of birth must be in the past")
        private LocalDate dob;

        @NotBlank(message = "Address is mandatory")
        private String address;

        @NotBlank(message = "PAN is mandatory")
        @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format")
        private String pan;

        @NotBlank(message = "Aadhaar number is mandatory")
        @Pattern(regexp = "^\\d{12}$", message = "Aadhaar must be 12 digits")
        private String aadhaar;

        private String aadhaarPhotoBase64;
        private String panPhotoBase64;
        private String passportPhotoBase64;

        // Getters and setters for CustomerData...
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

    // Getters and setters for the main request object
    public CustomerData getCustomer() { return customer; }
    public void setCustomer(CustomerData customer) { this.customer = customer; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // --- GETTER AND SETTER FOR THE FIX ---
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}