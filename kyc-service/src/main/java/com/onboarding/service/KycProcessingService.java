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

    private final KycApplicationRepository kycRepo;
    private final KafkaProducerService kafkaProducerService;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;

    public KycProcessingService(KycApplicationRepository kycRepo, KafkaProducerService kafka, AccountClient accClient, CustomerClient custClient) {
        this.kycRepo = kycRepo;
        this.kafkaProducerService = kafka;
        this.accountClient = accClient;
        this.customerClient = custClient;
    }

    @Transactional
    public void processKyc(Long applicationId, boolean isApproved, String rejectionReason) {
        KycApplication application = kycRepo.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("KYC Application not found with ID: " + applicationId));

        if (application.getKycStatus() != KycStatus.PENDING) {
            throw new IllegalStateException("This application has already been processed.");
        }

        KycStatusUpdateEvent emailEvent = new KycStatusUpdateEvent();
        emailEvent.setCustomerName(application.getFullName());
        emailEvent.setCustomerEmail(application.getEmail());

        if (isApproved) {
            LOGGER.info("Approving KYC for application ID {}.", applicationId);
            application.setKycStatus(KycStatus.VERIFIED);
            
            // 1. Send COMPLETE data to customer-service to create the permanent Customer and User
            CustomerDTO newCustomer = createApprovedCustomerInCustomerService(application);
            
            // 2. Create Inactive Account in account-service using the new customer's ID
            createInactiveAccountInAccountService(newCustomer.getId(), application);

            // 3. Activate the Account in account-service
            AccountDTO activeAccount = accountClient.activateAccount(newCustomer.getId());

            // 4. Populate event for the success email
            emailEvent.setKycStatus("VERIFIED");
            emailEvent.setAccountNumber(activeAccount.getAccountNumber());
            emailEvent.setAccountType(activeAccount.getAccountType());
            emailEvent.setIfscCode(activeAccount.getIfscCode());
        } else {
            LOGGER.warn("Rejecting KYC for application ID {} with reason: {}", applicationId, rejectionReason);
            application.setKycStatus(KycStatus.REJECTED);
            emailEvent.setKycStatus("REJECTED");
            emailEvent.setRejectionReason(rejectionReason);
        }
        
        kycRepo.save(application);
        kafkaProducerService.sendKycUpdateNotification(emailEvent);
    }

    /**
     * *** THIS IS THE FIRST FIX ***
     * This method now maps ALL fields from the KycApplication to the DTO.
     */
 // In KycProcessingService.java

    private CustomerDTO createApprovedCustomerInCustomerService(KycApplication app) {
        KycApplicationDataDTO kycData = new KycApplicationDataDTO();
        
        // This method now maps the service preferences as well
        kycData.setId(app.getId());
        kycData.setFullName(app.getFullName());
        kycData.setDob(app.getDob());
        kycData.setGender(app.getGender());
        kycData.setMaritalStatus(app.getMaritalStatus());
        kycData.setFathersName(app.getFathersName());
        kycData.setNationality(app.getNationality());
        kycData.setProfession(app.getProfession());
        kycData.setAddress(app.getAddress());
        kycData.setEmail(app.getEmail());
        kycData.setPhone(app.getPhone());
        kycData.setPan(app.getPan());
        kycData.setAadhaar(app.getAadhaar());
        kycData.setUsername(app.getUsername());
        kycData.setPassword(app.getPassword());
        
        // *** THE FIX: Add missing fields to the DTO ***
        kycData.setRequestedAccountType(app.getRequestedAccountType());
        kycData.setNetBankingEnabled(app.getNetBankingEnabled()); // Assuming this field exists on KycApplication now
        kycData.setDebitCardIssued(app.getDebitCardIssued());   // Assuming this field exists on KycApplication now
        kycData.setChequeBookIssued(app.getChequeBookIssued()); // Assuming this field exists on KycApplication now
        
        return customerClient.createApprovedCustomer(kycData);
    }
    
    private void createInactiveAccountInAccountService(Long customerId, KycApplication app) {
        Map<String, Object> creationData = new HashMap<>();
        creationData.put("customerId", customerId);
        creationData.put("accountType", app.getRequestedAccountType());

        // *** THE FIX: Use the actual values from the application ***
        creationData.put("netBankingEnabled", app.getNetBankingEnabled());
        creationData.put("debitCardIssued", app.getDebitCardIssued());
        creationData.put("chequeBookIssued", app.getChequeBookIssued());
        
        // This part requires adding a Nominee object to your KycApplication entity
        // For now, let's keep it as-is until you add that feature.
        creationData.put("nomineeRegistered", false); 
        creationData.put("nomineeName", null);

        accountClient.createInactiveAccount(creationData);
    }
    
    
  
}