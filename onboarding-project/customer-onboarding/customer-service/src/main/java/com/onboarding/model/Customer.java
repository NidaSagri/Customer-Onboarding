package com.onboarding.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Table(name = "CUSTOMERS")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is mandatory")
    private String fullName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phone;
    
    @NotNull(message = "Date of birth is mandatory")
    private LocalDate dob;
    
    @NotBlank(message = "Address is mandatory")
    private String address;

    @NotBlank(message = "PAN is mandatory")
    @Column(unique = true)
    private String pan;

    @NotBlank(message = "Aadhaar is mandatory")
    @Column(unique = true)
    private String aadhaar;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus = KycStatus.PENDING;
    
    // --- NEW FIELDS FOR IMAGE DATA ---
    @Lob
    @Column(name = "aadhaar_photo_base64", columnDefinition = "CLOB")
    private String aadhaarPhotoBase64;

    @Lob
    @Column(name = "pan_photo_base64", columnDefinition = "CLOB")
    private String panPhotoBase64;

    @Lob
    @Column(name = "passport_photo_base64", columnDefinition = "CLOB")
    private String passportPhotoBase64;

    // Getters and Setters for all fields...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }
    public String getAadhaarPhotoBase64() { return aadhaarPhotoBase64; }
    public void setAadhaarPhotoBase64(String aadhaarPhotoBase64) { this.aadhaarPhotoBase64 = aadhaarPhotoBase64; }
    public String getPanPhotoBase64() { return panPhotoBase64; }
    public void setPanPhotoBase64(String panPhotoBase64) { this.panPhotoBase64 = panPhotoBase64; }
    public String getPassportPhotoBase64() { return passportPhotoBase64; }
    public void setPassportPhotoBase64(String passportPhotoBase64) { this.passportPhotoBase64 = passportPhotoBase64; }
}