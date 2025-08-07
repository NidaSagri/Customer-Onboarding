package com.onboarding.service;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.dto.NewKycApplicationEvent;
import com.onboarding.exception.CustomerAlreadyExistsException;
import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus; // <-- Make sure this is imported
import com.onboarding.repository.KycApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class KycApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KycApplicationService.class);
    
    private final KycApplicationRepository kycRepo;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducer;

    public KycApplicationService(KycApplicationRepository kycRepo, PasswordEncoder encoder, KafkaProducerService kafka) {
        this.kycRepo = kycRepo;
        this.passwordEncoder = encoder;
        this.kafkaProducer = kafka;
    }

    @Transactional
    public KycApplication submitNewApplication(CustomerRegistrationRequest request) {
        // ... (your existing submitNewApplication logic is here)
        // This method is correct from our previous fix.
        LOGGER.info("Processing new KYC application submission for username: {}", request.getUsername());

        if (kycRepo.existsByPan(request.getCustomer().getPan())) throw new CustomerAlreadyExistsException("A user with this PAN number already exists.");
        if (kycRepo.existsByAadhaar(request.getCustomer().getAadhaar())) throw new CustomerAlreadyExistsException("A user with this Aadhaar number already exists.");
        if (kycRepo.existsByUsername(request.getUsername())) throw new CustomerAlreadyExistsException("This username is already taken. Please choose another.");
        if (kycRepo.existsByEmail(request.getCustomer().getEmail())) throw new CustomerAlreadyExistsException("A user with this email address already exists.");

        KycApplication app = new KycApplication();
        
        CustomerRegistrationRequest.CustomerData customerData = request.getCustomer();
        app.setFullName(customerData.getFullName());
        app.setFatherName(customerData.getFatherName());
        app.setMotherName(customerData.getMotherName());
        app.setGender(customerData.getGender());
        app.setMaritalStatus(customerData.getMaritalStatus());
        app.setNationality(customerData.getNationality());
        app.setProfession(customerData.getProfession());
        app.setEmail(customerData.getEmail());
        app.setPhone(customerData.getPhone());
        app.setDob(customerData.getDob());
        app.setAddress(customerData.getAddress());
        app.setPan(customerData.getPan());
        app.setAadhaar(customerData.getAadhaar());
        app.setAadhaarPhotoBase64(customerData.getAadhaarPhotoBase64());
        app.setPanPhotoBase64(customerData.getPanPhotoBase64());
        app.setPassportPhotoBase64(customerData.getPassportPhotoBase64());

        app.setUsername(request.getUsername());
        app.setPassword(passwordEncoder.encode(request.getPassword()));
        app.setPreferredAccountType(request.getAccountType());

        KycApplication savedApp = kycRepo.save(app);
        LOGGER.info("Successfully saved new KYC application with ID: {}", savedApp.getId());

        kafkaProducer.sendNewApplicationNotification(
            new NewKycApplicationEvent(savedApp.getId(), savedApp.getFullName(), savedApp.getEmail())
        );

        return savedApp;
    }

    // --- NEW AND UPDATED METHODS FOR THE DASHBOARD ---

    @Transactional(readOnly = true)
    public Page<KycApplication> findAllApplications(Pageable pageable) {
        return kycRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<KycApplication> findApplicationById(Long id) {
        return kycRepo.findById(id);
    }
    
    /**
     * Counts all applications in the database, regardless of status.
     * @return The total number of applications.
     */
    @Transactional(readOnly = true)
    public long countTotalApplications() {
        return kycRepo.count();
    }
    
    /**
     * Counts only the applications with a PENDING status.
     * @return The number of pending applications.
     */
    @Transactional(readOnly = true)
    public long countPendingApplications() {
        return kycRepo.countByKycStatus(KycStatus.PENDING);
    }
    
    /**
     * Counts applications that have been successfully VERIFIED.
     * This is the number that will populate the "Verified Customers" card.
     * @return The number of verified applications.
     */
    @Transactional(readOnly = true)
    public long countVerifiedApplications() {
        return kycRepo.countByKycStatus(KycStatus.VERIFIED);
    }
}