package com.onboarding.service;

import com.onboarding.dto.AccountDTO;
import com.onboarding.dto.CustomerDTO;
import com.onboarding.dto.KycApplicationDataDTO;
import com.onboarding.dto.KycStatusUpdateEvent;
import com.onboarding.feign.AccountClient;
import com.onboarding.feign.CustomerClient;
import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.repository.KycApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class KycProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycProcessingService.class);
    private static final String MOCK_IFSC_CODE = "OFSS0001234";

    private final KycApplicationRepository kycApplicationRepository;
    private final KafkaProducerService kafkaProducerService;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;

    public KycProcessingService(KycApplicationRepository kycAppRepo, KafkaProducerService kafka, AccountClient accClient, CustomerClient custClient) {
        this.kycApplicationRepository = kycAppRepo;
        this.kafkaProducerService = kafka;
        this.accountClient = accClient;
        this.customerClient = custClient;
    }

    @Transactional
    public void processKycApplication(Long applicationId, boolean isApproved, String rejectionReason) {
        KycApplication application = kycApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("KYC Application not found with ID: " + applicationId));

        if (application.getKycStatus() != KycStatus.PENDING) {
            throw new IllegalStateException("This KYC application has already been processed.");
        }

        KycStatusUpdateEvent event = new KycStatusUpdateEvent();
        String statusString;

        if (isApproved) {
            LOGGER.info("Approving KYC for application ID {}.", applicationId);
            application.setKycStatus(KycStatus.VERIFIED);
            statusString = "VERIFIED";

            // --- ORCHESTRATION WORKFLOW ---
            // 1. Send data to customer-service to create the final Customer and User
            CustomerDTO newCustomer = createApprovedCustomerInCustomerService(application);
            
            // 2. Send data to account-service to create the inactive bank account
            createInactiveAccountInAccountService(newCustomer.getId(), application.getRequestedAccountType());

            // 3. Send data to account-service to ACTIVATE the account
            AccountDTO activeAccount = activateAccountInAccountService(newCustomer.getId());

            // 4. Populate event for the rich email
            event.setAccountNumber(activeAccount.getAccountNumber());
            event.setAccountType(activeAccount.getAccountType());
            event.setIfscCode(MOCK_IFSC_CODE);
        } else {
            LOGGER.warn("Rejecting KYC for application ID {} with reason: {}", applicationId, rejectionReason);
            application.setKycStatus(KycStatus.REJECTED);
            statusString = "REJECTED";
            event.setRejectionReason(rejectionReason);
        }
        
        kycApplicationRepository.save(application);

        event.setCustomerName(application.getFullName());
        event.setCustomerEmail(application.getEmail());
        event.setKycStatus(statusString);
        kafkaProducerService.sendKycUpdateNotification(event);
    }
    
    // --- Helper methods for Feign calls ---
    private CustomerDTO createApprovedCustomerInCustomerService(KycApplication app) {
        KycApplicationDataDTO kycData = new KycApplicationDataDTO();
        kycData.setKycApplicationId(app.getId());
        kycData.setFullName(app.getFullName());
        kycData.setEmail(app.getEmail());
        kycData.setPhone(app.getPhone());
        kycData.setDob(app.getDob());
        kycData.setAddress(app.getAddress());
        kycData.setPan(app.getPan());
        kycData.setAadhaar(app.getAadhaar());
        kycData.setUsername(app.getUsername());
        kycData.setPassword(app.getPassword()); // Send the encrypted password
        try {
            return customerClient.createApprovedCustomer(kycData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create approved customer in customer-service: " + e.getMessage());
        }
    }

    private AccountDTO createInactiveAccountInAccountService(Long customerId, String accountType) {
        try {
            Map<String, Object> creationData = new HashMap<>();
            creationData.put("customerId", customerId);
            creationData.put("accountType", accountType);
            return accountClient.createInactiveAccount(creationData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create inactive account in account-service: " + e.getMessage());
        }
    }

    private AccountDTO activateAccountInAccountService(Long customerId) {
        try {
            return accountClient.activateAccount(customerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to activate account in account-service: " + e.getMessage());
        }
    }
}