package com.onboarding.service;

import com.onboarding.dto.AccountDTO;
import com.onboarding.dto.KycStatusUpdateEvent;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KycService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycService.class);
    private static final String MOCK_IFSC_CODE = "OFSS0001234";

    private final CustomerRepository customerRepository;
    private final KafkaProducerService kafkaProducerService;
    private final AccountClient accountClient;

    public KycService(CustomerRepository customerRepository, KafkaProducerService kafkaProducerService, AccountClient accountClient) {
        this.customerRepository = customerRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.accountClient = accountClient;
    }

    @Transactional
    public void verifyKyc(Long customerId, boolean isVerified) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        String statusString;
        KycStatusUpdateEvent event = new KycStatusUpdateEvent();

        if (isVerified) {
            customer.setKycStatus(KycStatus.VERIFIED);
            statusString = "VERIFIED";
            
            LOGGER.info("KYC approved for customer {}. Attempting to activate account...", customer.getFullName());
            try {
                AccountDTO account = accountClient.activateAccount(customerId);
                LOGGER.info("Successfully activated account {} for customer {}", account.getAccountNumber(), customer.getFullName());
                
                event.setAccountNumber(account.getAccountNumber());
                event.setAccountType(account.getAccountType());
                event.setIfscCode(MOCK_IFSC_CODE);
            } catch (Exception e) {
                LOGGER.error("CRITICAL: Failed to activate account for customer ID {}. Error: {}", customerId, e.getMessage());
            }
        } else {
            customer.setKycStatus(KycStatus.REJECTED);
            statusString = "REJECTED";
            LOGGER.warn("KYC rejected for customer {}", customer.getFullName());
        }
        
        customerRepository.save(customer);

        event.setCustomerName(customer.getFullName());
        event.setCustomerEmail(customer.getEmail());
        event.setKycStatus(statusString);
        kafkaProducerService.sendKycUpdateNotification(event);
    }
}