package com.onboarding.service;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.dto.NewCustomerEvent;
import com.onboarding.exception.CustomerAlreadyExistsException;
// We DO NOT import AccountClient here
import com.onboarding.model.KycApplication;
import com.onboarding.repository.KycApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    private final KycApplicationRepository kycApplicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    // The AccountClient dependency has been REMOVED

    // --- CORRECTED CONSTRUCTOR ---
    public CustomerService(KycApplicationRepository kycApplicationRepository, PasswordEncoder passwordEncoder, KafkaProducerService kafkaProducerService) {
        this.kycApplicationRepository = kycApplicationRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducerService = kafkaProducerService;
    }
    
    @Transactional
    public KycApplication registerKycApplication(CustomerRegistrationRequest request) {
        LOGGER.info("Processing new KYC application for user: {}", request.getUsername());

        CustomerRegistrationRequest.CustomerData customerData = request.getCustomer();

        // Validation logic (this is correct)...
        
        KycApplication application = new KycApplication();
        
        // --- THE FIX: Ensure every single field is copied ---
        application.setFullName(customerData.getFullName());
        application.setFatherName(customerData.getFatherName());
        application.setMotherName(customerData.getMotherName()); // <-- Critical line
        application.setGender(customerData.getGender());
        application.setMaritalStatus(customerData.getMaritalStatus());
        application.setNationality(customerData.getNationality());
        application.setProfession(customerData.getProfession());
        application.setEmail(customerData.getEmail());
        application.setPhone(customerData.getPhone());
        application.setDob(customerData.getDob());
        application.setAddress(customerData.getAddress());
        application.setPan(customerData.getPan());
        application.setAadhaar(customerData.getAadhaar());
        application.setAadhaarPhotoBase64(customerData.getAadhaarPhotoBase64());
        application.setPanPhotoBase64(customerData.getPanPhotoBase64());
        application.setPassportPhotoBase64(customerData.getPassportPhotoBase64());
        application.setPreferredAccountType(request.getAccountType());
        application.setUsername(request.getUsername());
        application.setPassword(passwordEncoder.encode(request.getPassword()));

        KycApplication savedApplication = kycApplicationRepository.save(application);
        LOGGER.info("Successfully saved new KYC application with ID: {}", savedApplication.getId());
        
        // Publish an event to notify the admin
        NewCustomerEvent event = new NewCustomerEvent(
            savedApplication.getId(),
            savedApplication.getFullName(),
            savedApplication.getEmail()
        );
        kafkaProducerService.sendNewCustomerNotification(event);

        return savedApplication;
    }
}