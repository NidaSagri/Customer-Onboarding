package com.onboarding.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is mandatory")
    private String fullName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
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



	public KycStatus getKycStatus() {
		return kycStatus;
	}



	public void setKycStatus(KycStatus kycStatus) {
		this.kycStatus = kycStatus;
	}



	// KYC Status for this customer
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus = KycStatus.PENDING;
}