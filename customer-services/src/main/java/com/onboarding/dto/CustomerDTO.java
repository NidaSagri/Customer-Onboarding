package com.onboarding.dto;
import java.time.LocalDate;

public class CustomerDTO {
    private Long id;
    private String fullName;
    private String fatherName;
    private String motherName;
    private String email;
    private String phone;
    private LocalDate dob;
    private String address;
    private String gender;
    private String maritalStatus;
    private String nationality;
    private String profession;
    private String pan;
    private String aadhaar;
    private String kycStatus;
    private String aadhaarPhotoBase64;
    private String panPhotoBase64;
    private String passportPhotoBase64;

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getFatherName() { return fatherName; }
    public String getMotherName() { return motherName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getDob() { return dob; }
    public String getAddress() { return address; }
    public String getGender() { return gender; }
    public String getMaritalStatus() { return maritalStatus; }
    public String getNationality() { return nationality; }
    public String getProfession() { return profession; }
    public String getPan() { return pan; }
    public String getAadhaar() { return aadhaar; }
    public String getKycStatus() { return kycStatus; }
    public String getAadhaarPhotoBase64() { return aadhaarPhotoBase64; }
    public String getPanPhotoBase64() { return panPhotoBase64; }
    public String getPassportPhotoBase64() { return passportPhotoBase64; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public void setAddress(String address) { this.address = address; }
    public void setGender(String gender) { this.gender = gender; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public void setProfession(String profession) { this.profession = profession; }
    public void setPan(String pan) { this.pan = pan; }
    public void setAadhaar(String aadhaar) { this.aadhaar = aadhaar; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
    public void setAadhaarPhotoBase64(String aadhaarPhotoBase64) { this.aadhaarPhotoBase64 = aadhaarPhotoBase64; }
    public void setPanPhotoBase64(String panPhotoBase64) { this.panPhotoBase64 = panPhotoBase64; }
    public void setPassportPhotoBase64(String passportPhotoBase64) { this.passportPhotoBase64 = passportPhotoBase64; }
}