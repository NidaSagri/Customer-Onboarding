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

        // --- Validation against the staging table ---
        if (kycApplicationRepository.existsByPan(request.getCustomer().getPan())) {
            throw new CustomerAlreadyExistsException("An application with this PAN number already exists.");
        }
        if (kycApplicationRepository.existsByAadhaar(request.getCustomer().getAadhaar())) {
            throw new CustomerAlreadyExistsException("An application with this Aadhaar number already exists.");
        }
        if (kycApplicationRepository.existsByUsername(request.getUsername())) {
            throw new CustomerAlreadyExistsException("An application with this username already exists.");
        }
        if (kycApplicationRepository.existsByEmail(request.getCustomer().getEmail())) {
            throw new CustomerAlreadyExistsException("An application with this email address already exists.");
        }

        KycApplication application = new KycApplication();
        
        // Copy all data from the request to the new entity
        application.setFullName(request.getCustomer().getFullName());
        application.setFatherName(request.getCustomer().getFatherName());
        application.setMotherName(request.getCustomer().getMotherName());
        application.setGender(request.getCustomer().getGender());
        application.setMaritalStatus(request.getCustomer().getMaritalStatus());
        application.setNationality(request.getCustomer().getNationality());
        application.setProfession(request.getCustomer().getProfession());
        application.setEmail(request.getCustomer().getEmail());
        application.setPhone(request.getCustomer().getPhone());
        application.setDob(request.getCustomer().getDob());
        application.setAddress(request.getCustomer().getAddress());
        application.setPan(request.getCustomer().getPan());
        application.setAadhaar(request.getCustomer().getAadhaar());
        application.setAadhaarPhotoBase64(request.getCustomer().getAadhaarPhotoBase64());
        application.setPanPhotoBase64(request.getCustomer().getPanPhotoBase64());
        application.setPassportPhotoBase64(request.getCustomer().getPassportPhotoBase64());
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