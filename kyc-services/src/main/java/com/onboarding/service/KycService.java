package com.onboarding.service;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.dto.KycStatusUpdateEvent;
import com.onboarding.feign.CustomerClient;
import com.onboarding.model.*;
import com.onboarding.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class KycService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycService.class);

    private final KycApplicationRepository kycApplicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerClient customerClient;
    private final KafkaProducerService kafkaProducerService;

    public KycService(KycApplicationRepository kycRepo, UserRepository userRepo, RoleRepository roleRepo, CustomerClient client, KafkaProducerService kafkaSvc) {
        this.kycApplicationRepository = kycRepo;
        this.userRepository = userRepo;
        this.roleRepository = roleRepo;
        this.customerClient = client;
        this.kafkaProducerService = kafkaSvc;
    }

    @Transactional
    public void processKycApplication(Long applicationId, boolean isApproved, String rejectionReason) {
        KycApplication app = kycApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("KYC Application not found: " + applicationId));

        if (app.getKycStatus() != KycStatus.PENDING) {
            throw new IllegalStateException("Application has already been processed.");
        }

        if (isApproved) {
            approveApplication(app);
        } else {
            rejectApplication(app, rejectionReason);
        }
    }

    private void approveApplication(KycApplication app) {
        LOGGER.info("Approving KYC application ID: {}", app.getId());
        
        CustomerDTO customerDto = mapToCustomerDTO(app);

        // 1. Call customer-service to create the permanent customer record
        CustomerDTO createdCustomer;
        try {
            createdCustomer = customerClient.createCustomer(customerDto).getBody();
            if (createdCustomer == null || createdCustomer.getId() == null) {
                throw new IllegalStateException("Failed to create customer record in remote service.");
            }
            LOGGER.info("Successfully created permanent customer record with ID: {}", createdCustomer.getId());
        } catch (Exception e) {
            LOGGER.error("CRITICAL: Failed to call customer-service for application ID {}. Error: {}", app.getId(), e.getMessage());
            throw new RuntimeException("Customer creation failed after KYC approval. Please contact support.");
        }

        // 2. Create the User login in this service's DB
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found."));
        User user = new User();
        user.setUsername(app.getUsername());
        user.setPassword(app.getPassword());
        user.setRoles(Set.of(customerRole));
        user.setCustomerId(createdCustomer.getId()); // Link to the remote customer ID
        userRepository.save(user);

        // 3. Update local application status
        app.setKycStatus(KycStatus.VERIFIED);
        kycApplicationRepository.save(app);

        // 4. Send notification
        publishStatusUpdate(app.getFullName(), app.getEmail(), "VERIFIED", null);
    }

    private void rejectApplication(KycApplication app, String reason) {
        LOGGER.warn("Rejecting KYC application ID: {} with reason: {}", app.getId(), reason);
        app.setKycStatus(KycStatus.REJECTED);
        app.setRejectionReason(reason);
        kycApplicationRepository.save(app);
        publishStatusUpdate(app.getFullName(), app.getEmail(), "REJECTED", reason);
    }

    private void publishStatusUpdate(String name, String email, String status, String reason) {
        KycStatusUpdateEvent event = new KycStatusUpdateEvent();
        event.setCustomerName(name);
        event.setCustomerEmail(email);
        event.setKycStatus(status);
        event.setRejectionReason(reason);
        kafkaProducerService.sendKycUpdateNotification(event);
    }
    
    private CustomerDTO mapToCustomerDTO(KycApplication app) {
        CustomerDTO dto = new CustomerDTO();
        // ... map all fields from app to dto ...
        dto.setFullName(app.getFullName());
        // ... etc ...
        dto.setKycStatus(KycStatus.VERIFIED.name());
        return dto;
    }
}