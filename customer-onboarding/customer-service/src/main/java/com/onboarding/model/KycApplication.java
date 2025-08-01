package com.onboarding.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "KYC_APPLICATIONS")
public class KycApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Personal Information ---
    @NotBlank private String fullName;
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp = "^\\d{10}$") private String phone;
    @NotNull private LocalDate dob;
    @NotBlank private String address;
    @NotBlank private String fatherName; // New Field
    @NotBlank private String motherName; // New Field

    // --- Identity Information ---
    @NotBlank @Column(unique = true) private String pan;
    @NotBlank @Column(unique = true) private String aadhaar;
    
 // --- NEW DEMOGRAPHIC FIELDS ---
    @NotBlank private String gender;
    @NotBlank private String maritalStatus;
    @NotBlank private String nationality;
    @NotBlank private String profession;

    // --- Documents (Base64) ---
    @Lob @Column(columnDefinition = "CLOB") private String aadhaarPhotoBase64;
    @Lob @Column(columnDefinition = "CLOB") private String panPhotoBase64;
    @Lob @Column(columnDefinition = "CLOB") private String passportPhotoBase64;

    // --- Account Preference ---
    @NotBlank private String preferredAccountType; // e.g., "SAVINGS"

    // --- Login Credentials ---
    @NotBlank @Column(unique = true) private String username;
    @NotBlank private String password; // This will be encrypted

    // --- Application Status ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus = KycStatus.PENDING;
    private String rejectionReason;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public LocalDate getDob() {
		return dob;
	}
	public void setDob(LocalDate dob) {
		this.dob = dob;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getFatherName() {
		return fatherName;
	}
	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}
	public String getMotherName() {
		return motherName;
	}
	public void setMotherName(String motherName) {
		this.motherName = motherName;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public String getAadhaar() {
		return aadhaar;
	}
	public void setAadhaar(String aadhaar) {
		this.aadhaar = aadhaar;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getMaritalStatus() {
		return maritalStatus;
	}
	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}
	public String getNationality() {
		return nationality;
	}
	public void setNationality(String nationality) {
		this.nationality = nationality;
	}
	public String getProfession() {
		return profession;
	}
	public void setProfession(String profession) {
		this.profession = profession;
	}
	public String getAadhaarPhotoBase64() {
		return aadhaarPhotoBase64;
	}
	public void setAadhaarPhotoBase64(String aadhaarPhotoBase64) {
		this.aadhaarPhotoBase64 = aadhaarPhotoBase64;
	}
	public String getPanPhotoBase64() {
		return panPhotoBase64;
	}
	public void setPanPhotoBase64(String panPhotoBase64) {
		this.panPhotoBase64 = panPhotoBase64;
	}
	public String getPassportPhotoBase64() {
		return passportPhotoBase64;
	}
	public void setPassportPhotoBase64(String passportPhotoBase64) {
		this.passportPhotoBase64 = passportPhotoBase64;
	}
	public String getPreferredAccountType() {
		return preferredAccountType;
	}
	public void setPreferredAccountType(String preferredAccountType) {
		this.preferredAccountType = preferredAccountType;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public KycStatus getKycStatus() {
		return kycStatus;
	}
	public void setKycStatus(KycStatus kycStatus) {
		this.kycStatus = kycStatus;
	}
	public String getRejectionReason() {
		return rejectionReason;
	}
	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
	
    
    
}