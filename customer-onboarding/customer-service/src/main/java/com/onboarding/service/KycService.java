package com.onboarding.service;

import com.onboarding.dto.AccountCreationRequest;
import com.onboarding.dto.AccountDTO;
import com.onboarding.dto.KycStatusUpdateEvent;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.Customer;
import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.model.Role;
import com.onboarding.model.User;
import com.onboarding.repository.CustomerRepository;
import com.onboarding.repository.KycApplicationRepository;
import com.onboarding.repository.RoleRepository;
import com.onboarding.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class KycService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycService.class);

    private final KycApplicationRepository kycApplicationRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountClient accountClient;
    private final KafkaProducerService kafkaProducerService;

    public KycService(KycApplicationRepository kycApplicationRepository, CustomerRepository customerRepository, UserRepository userRepository, RoleRepository roleRepository, AccountClient accountClient, KafkaProducerService kafkaProducerService) {
        this.kycApplicationRepository = kycApplicationRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountClient = accountClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * The core business logic for approving or rejecting a KYC application. When an
     * application is approved, it promotes the staged data into permanent Customer,
     * User, and Account records.
     */
    @Transactional
    public void processKycApplication(Long applicationId, boolean isApproved, String rejectionReason) {
        KycApplication application = kycApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("KYC Application not found with ID: " + applicationId));

        KycStatusUpdateEvent event = new KycStatusUpdateEvent();
        event.setCustomerName(application.getFullName());
        event.setCustomerEmail(application.getEmail());

        if (isApproved) {
            LOGGER.info("Approving KYC application ID: {}", applicationId);
            application.setKycStatus(KycStatus.VERIFIED);
            
            // 1. Promote to a permanent Customer record
            Customer customer = new Customer();
            
            // --- THE FIX: Copy ALL fields from the application to the new customer ---
            customer.setFullName(application.getFullName());
            customer.setFatherName(application.getFatherName());
            customer.setEmail(application.getEmail());
            customer.setPhone(application.getPhone());
            customer.setDob(application.getDob());
            customer.setAddress(application.getAddress());
            customer.setGender(application.getGender());
            customer.setMaritalStatus(application.getMaritalStatus());
            customer.setNationality(application.getNationality());
            customer.setProfession(application.getProfession());
            customer.setPan(application.getPan());
            customer.setAadhaar(application.getAadhaar());
            customer.setAadhaarPhotoBase64(application.getAadhaarPhotoBase64());
            customer.setPanPhotoBase64(application.getPanPhotoBase64());
            customer.setPassportPhotoBase64(application.getPassportPhotoBase64());
            customer.setKycStatus(KycStatus.VERIFIED);
            
            // 2. Create the User login
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found."));
            User user = new User();
            user.setUsername(application.getUsername());
            user.setPassword(application.getPassword()); // Password is already encrypted
            user.setRoles(Set.of(customerRole));
            user.setCustomer(customer);
            
            // Saving the User will cascade and save the new Customer as well
            User savedUser = userRepository.save(user);
            
            // 3. Call account-service to create and activate the account
            try {
                AccountCreationRequest accountRequest = new AccountCreationRequest();
                accountRequest.setCustomerId(savedUser.getCustomer().getId());
                accountRequest.setAccountType(application.getPreferredAccountType());
                
                AccountDTO createdAccount = accountClient.createActiveAccount(accountRequest);
                
                // 4. Populate the Kafka event with the new account details for the email
                event.setKycStatus("VERIFIED");
                event.setAccountNumber(createdAccount.getAccountNumber());
                event.setAccountType(createdAccount.getAccountType());
                
            } catch (Exception e) {
                LOGGER.error("CRITICAL: KYC approved but failed to create bank account for application ID {}. Error: {}", applicationId, e.getMessage());
                // In a real system, we would need a compensating transaction to roll back the customer creation.
                throw new RuntimeException("Account creation failed after KYC approval. Please contact support.");
            }

        } else { // Rejected
            LOGGER.warn("Rejecting KYC application ID: {} with reason: {}", applicationId, rejectionReason);
            application.setKycStatus(KycStatus.REJECTED);
            application.setRejectionReason(rejectionReason);
            
            event.setKycStatus("REJECTED");
            event.setRejectionReason(rejectionReason);
        }

        kycApplicationRepository.save(application); // Save the final status of the application
        kafkaProducerService.sendKycUpdateNotification(event); // Send the email notification
    }
}