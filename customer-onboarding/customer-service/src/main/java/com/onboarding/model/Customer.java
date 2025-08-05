package com.onboarding.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CUSTOMERS")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    @Column(unique = true) private String email;
    private String phone;
    private LocalDate dob;
    private String address;
    @Column(unique = true) private String pan;
    @Column(unique = true) private String aadhaar;
    
    @Enumerated(EnumType.STRING)
    private final KycStatus kycStatus = KycStatus.VERIFIED;
    
    private Long kycApplicationId;

    // Getters and Setters...
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
    public Long getKycApplicationId() { return kycApplicationId; }
    public void setKycApplicationId(Long kycApplicationId) { this.kycApplicationId = kycApplicationId; }
}