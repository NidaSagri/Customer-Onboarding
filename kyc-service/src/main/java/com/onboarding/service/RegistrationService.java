package com.onboarding.service;

import com.onboarding.dto.FullRegistrationRequest;
import com.onboarding.dto.NewKycApplicationEvent;
import com.onboarding.dto.NomineeDTO;
import com.onboarding.exception.CustomerAlreadyExistsException;
import com.onboarding.model.KycApplication;
import com.onboarding.model.KycNominee;
import com.onboarding.repository.KycApplicationRepository;
import com.onboarding.repository.KycNomineeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;

@Service
public class RegistrationService {

    private final KycApplicationRepository kycRepo;
    private final KycNomineeRepository kycNomineeRepo;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;

    public RegistrationService(KycApplicationRepository kycRepo, KycNomineeRepository kycNomineeRepo, PasswordEncoder passwordEncoder, KafkaProducerService kafkaProducerService) {
        this.kycRepo = kycRepo;
        this.kycNomineeRepo = kycNomineeRepo;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public void processRegistration(FullRegistrationRequest request, MultipartFile passportPhoto, MultipartFile panDoc, MultipartFile aadhaarDoc) throws IOException {
        validateUniqueness(request);

        KycApplication application = new KycApplication();
        mapRequestToApplication(application, request);
        processDocuments(application, passportPhoto, panDoc, aadhaarDoc);
        application.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Handle nominee creation if the user provided the details
        if (request.getNominee() != null && request.getNominee().getName() != null && !request.getNominee().getName().isEmpty()) {
            handleNominee(application, request.getNominee());
        }

        KycApplication savedApplication = kycRepo.save(application);
        
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
        
        // Check for duplicate nominee Aadhaar number
        if (request.getNominee() != null && request.getNominee().getAadhaarNumber() != null && !request.getNominee().getAadhaarNumber().isBlank()) {
            kycNomineeRepo.findByAadhaarNumber(request.getNominee().getAadhaarNumber()).ifPresent(n -> {
                throw new CustomerAlreadyExistsException("A nominee with that Aadhaar number already exists.");
            });
        }
    }

    /**
     * *** THE FIX IS APPLIED HERE ***
     * This method now maps the service preference booleans from the request DTO to the entity.
     */
//    private void mapRequestToApplication(KycApplication app, FullRegistrationRequest req) {
//        // Map all personal, contact, and login details
//        app.setFullName(req.getFullname());
//        app.setEmail(req.getEmail());
//        app.setPhone(req.getPhone());
//        app.setDob(req.getDob());
//        app.setAddress(req.getAddress());
//        app.setGender(req.getGender());
//        app.setMaritalStatus(req.getMaritalStatus());
//        app.setFathersName(req.getFathersName());
//        app.setNationality(req.getNationality());
//        app.setProfession(req.getProfession());
//        app.setPan(req.getPan());
//        app.setAadhaar(req.getAadhaar());
//        app.setUsername(req.getUsername());
//        app.setRequestedAccountType(req.getRequestedAccountType());
//        
//        // Map service preferences
//        app.setNetBankingEnabled(req.isNetBankingEnabled());
//        app.setDebitCardIssued(req.isDebitCardIssued());
//        app.setChequeBookIssued(req.isChequeBookIssued());
//        // Note: Password is set separately after encoding in the main method
//    }
    private void mapRequestToApplication(KycApplication app, FullRegistrationRequest req) {
        // Map all personal, contact, and login details
        app.setFullName(req.getFullname());
        app.setEmail(req.getEmail());
        app.setPhone(req.getPhone());
        app.setDob(req.getDob());
        app.setAddress(req.getAddress());
        app.setGender(req.getGender());
        app.setMaritalStatus(req.getMaritalStatus());
        
        // *** ADD THIS LINE ***
        app.setFathersName(req.getFathersName());
        
        app.setNationality(req.getNationality());
        app.setProfession(req.getProfession());
        app.setPan(req.getPan());
        app.setAadhaar(req.getAadhaar());
        app.setUsername(req.getUsername());

        // *** AND ADD THIS LINE ***
        app.setRequestedAccountType(req.getRequestedAccountType());
        
        // Map service preferences
        app.setNetBankingEnabled(req.isNetBankingEnabled());
        app.setDebitCardIssued(req.isDebitCardIssued());
        app.setChequeBookIssued(req.isChequeBookIssued());
    }
    // Helper method to create and link the nominee entity
    private void handleNominee(KycApplication application, NomineeDTO nomineeDTO) {
        KycNominee nominee = new KycNominee();
        nominee.setName(nomineeDTO.getName());
        nominee.setMobile(nomineeDTO.getMobile());
        nominee.setAddress(nomineeDTO.getAddress());
        nominee.setAadhaarNumber(nomineeDTO.getAadhaarNumber());
        application.setKycNominee(nominee); // This uses the helper method to set the bidirectional link
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