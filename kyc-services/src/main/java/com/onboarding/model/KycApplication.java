package com.onboarding.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Represents a user's initial application for KYC (Know Your Customer).
 * This entity holds all the data submitted during registration and tracks its
 * status (PENDING, VERIFIED, REJECTED) through the review process.
 */
@Entity
@Table(name = "KYC_APPLICATIONS")
public class KycApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Personal Information ---
    @NotBlank(message = "Full name cannot be blank")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Father's name cannot be blank")
    @Column(nullable = false)
    private String fatherName;

    @NotBlank(message = "Mother's name cannot be blank")
    @Column(nullable = false)
    private String motherName;

    @NotNull(message = "Date of birth cannot be null")
    @Column(nullable = false)
    private LocalDate dob;
    
    @NotBlank(message = "Gender cannot be blank")
    @Column(nullable = false)
    private String gender;

    @NotBlank(message = "Marital status cannot be blank")
    @Column(nullable = false)
    private String maritalStatus;

    @NotBlank(message = "Nationality cannot be blank")
    @Column(nullable = false)
    private String nationality;

    @NotBlank(message = "Profession cannot be blank")
    @Column(nullable = false)
    private String profession;

    // --- Identity & Contact ---
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Must be a valid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    @Column(nullable = false)
    private String phone;

    @NotBlank(message = "Address cannot be blank")
    @Column(nullable = false)
    private String address;

    @NotBlank(message = "PAN cannot be blank")
    @Column(nullable = false, unique = true)
    private String pan;

    @NotBlank(message = "Aadhaar cannot be blank")
    @Column(nullable = false, unique = true)
    private String aadhaar;

    // --- Login & Account Preferences ---
    @NotBlank(message = "Username cannot be blank")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password; // This will be encrypted

    @NotBlank(message = "Preferred account type cannot be blank")
    @Column(nullable = false)
    private String preferredAccountType;

    // --- Documents (Stored as Base64 encoded strings) ---
    @Lob // Large Object annotation, suitable for long text/CLOB
    @Column(name = "aadhaar_photo_base64", columnDefinition = "CLOB")
    private String aadhaarPhotoBase64;

    @Lob
    @Column(name = "pan_photo_base64", columnDefinition = "CLOB")
    private String panPhotoBase64;

    @Lob
    @Column(name = "passport_photo_base64", columnDefinition = "CLOB")
    private String passportPhotoBase64;

    // --- Application Status ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column
    private String rejectionReason;

    // --- Getters and Setters for all fields ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
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
    public String getPreferredAccountType() { return preferredAccountType; }
    public void setPreferredAccountType(String preferredAccountType) { this.preferredAccountType = preferredAccountType; }
    public String getAadhaarPhotoBase64() { return aadhaarPhotoBase64; }
    public void setAadhaarPhotoBase64(String aadhaarPhotoBase64) { this.aadhaarPhotoBase64 = aadhaarPhotoBase64; }
    public String getPanPhotoBase64() { return panPhotoBase64; }
    public void setPanPhotoBase64(String panPhotoBase64) { this.panPhotoBase64 = panPhotoBase64; }
    public String getPassportPhotoBase64() { return passportPhotoBase64; }
    public void setPassportPhotoBase64(String passportPhotoBase64) { this.passportPhotoBase64 = passportPhotoBase64; }
    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}