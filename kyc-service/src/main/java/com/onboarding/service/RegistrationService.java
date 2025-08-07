package com.onboarding.service;

import com.onboarding.dto.FullRegistrationRequest;
import com.onboarding.dto.NewKycApplicationEvent; // We will use your existing event DTO
import com.onboarding.exception.CustomerAlreadyExistsException;
import com.onboarding.model.KycApplication;
import com.onboarding.repository.KycApplicationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;

@Service
public class RegistrationService {

    private final KycApplicationRepository kycRepo;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;

    // Remove the CustomerClient dependency
    public RegistrationService(KycApplicationRepository kycRepo, PasswordEncoder passwordEncoder, KafkaProducerService kafkaProducerService) {
        this.kycRepo = kycRepo;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public void processRegistration(FullRegistrationRequest request, MultipartFile passportPhoto, MultipartFile panDoc, MultipartFile aadhaarDoc) throws IOException {
        // Step 1: Validate for uniqueness within the KYC applications
        validateUniqueness(request);

        // Step 2: Create and map the KycApplication entity
        KycApplication application = new KycApplication();
        mapRequestToApplication(application, request);
        
        // Step 3: Handle document uploads
        processDocuments(application, passportPhoto, panDoc, aadhaarDoc);
        
        // Step 4: Encrypt password
        application.setPassword(passwordEncoder.encode(request.getPassword()));

        // Step 5: Save the KycApplication locally
        KycApplication savedApplication = kycRepo.save(application);
        
        // Step 6: Notify admin about the new application via Kafka
        NewKycApplicationEvent event = new NewKycApplicationEvent(
            savedApplication.getId(),
            savedApplication.getFullName(),
            savedApplication.getEmail()
        );
        kafkaProducerService.sendNewKycApplicationNotification(event);
    }

    private void validateUniqueness(FullRegistrationRequest request) {
        kycRepo.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new CustomerAlreadyExistsException("Username '" + request.getUsername() + "' is already taken.");
        });
        kycRepo.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new CustomerAlreadyExistsException("Email '" + request.getEmail() + "' is already registered.");
        });
        kycRepo.findByPan(request.getPan()).ifPresent(u -> {
            throw new CustomerAlreadyExistsException("PAN '" + request.getPan() + "' is already registered.");
        });
        kycRepo.findByAadhaar(request.getAadhaar()).ifPresent(u -> {
            throw new CustomerAlreadyExistsException("Aadhaar '" + request.getAadhaar() + "' is already registered.");
        });
    }

    private void mapRequestToApplication(KycApplication app, FullRegistrationRequest req) {
        // Map all fields from the request DTO to the KycApplication entity
        app.setFullName(req.getFullname());
        app.setEmail(req.getEmail());
        app.setPhone(req.getPhone());
        app.setDob(req.getDob());
        app.setAddress(req.getAddress());
        app.setGender(req.getGender());
        app.setMaritalStatus(req.getMaritalStatus());
        app.setFathersName(req.getFathersName());
        app.setNationality(req.getNationality());
        app.setProfession(req.getProfession());
        app.setPan(req.getPan());
        app.setAadhaar(req.getAadhaar());
        app.setUsername(req.getUsername());
        app.setRequestedAccountType(req.getRequestedAccountType());
        // Note: Password is set separately after encoding
    }

    private void processDocuments(KycApplication app, MultipartFile passport, MultipartFile pan, MultipartFile aadhaar) throws IOException {
        if (passport != null && !passport.isEmpty()) {
            app.setPassportPhotoBase64("data:" + passport.getContentType() + ";base64," + Base64.getEncoder().encodeToString(passport.getBytes()));
        }
        if (pan != null && !pan.isEmpty()) {
            app.setPanPhotoBase64("data:" + pan.getContentType() + ";base64," + Base64.getEncoder().encodeToString(pan.getBytes()));
        }
        if (aadhaar != null && !aadhaar.isEmpty()) {
            app.setAadhaarPhotoBase64("data:" + aadhaar.getContentType() + ";base64," + Base64.getEncoder().encodeToString(aadhaar.getBytes()));
        }
    }
}