package com.onboarding.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CUSTOMERS")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Personal Information ---
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String fatherName;

    @Column(nullable = false)
    private String motherName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false)
    private LocalDate dob;
    
    @Column(nullable = false)
    private String address;
    
 // --- NEW DEMOGRAPHIC FIELDS ---
    @Column(nullable = false) private String gender;
    @Column(nullable = false) private String maritalStatus;
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
	@Column(nullable = false) private String nationality;
    @Column(nullable = false) private String profession;

    // --- Identity Information ---
    @Column(nullable = false, unique = true)
    private String pan;

    @Column(nullable = false, unique = true)
    private String aadhaar;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus; // This will always be VERIFIED for records in this table
    
    // --- Documents (Base64 Strings) ---
    @Lob
    @Column(name = "aadhaar_photo_base64", columnDefinition = "CLOB")
    private String aadhaarPhotoBase64;

    @Lob
    @Column(name = "pan_photo_base64", columnDefinition = "CLOB")
    private String panPhotoBase64;

    @Lob
    @Column(name = "passport_photo_base64", columnDefinition = "CLOB")
    private String passportPhotoBase64;

    // --- Relationship to the login User ---
    // This Customer is referenced by one User. Deleting the User will delete this Customer.
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;

    // Getters and Setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
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
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}