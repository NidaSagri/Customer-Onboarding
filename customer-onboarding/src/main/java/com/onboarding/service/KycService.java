package com.onboarding.service;

import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KycService {

    // Dependencies are declared as 'final'
    private final CustomerRepository customerRepository;
    // If you add KycDocumentRepository here, it must also be in the constructor

    // Constructor-based dependency injection. Spring will automatically provide the beans.
    public KycService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Verifies or rejects the KYC status for a given customer.
     * This method is transactional, meaning it will roll back if the save fails.
     *
     * @param customerId The ID of the customer to verify.
     * @param isVerified A boolean indicating approval (true) or rejection (false).
     */
    @Transactional
    public void verifyKyc(Long customerId, boolean isVerified) {
        // Find the customer or throw an exception if not found.
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        // Update the KYC status based on the admin's decision.
        if (isVerified) {
            customer.setKycStatus(KycStatus.VERIFIED);
            
            // Here you would trigger the account creation or notify the customer.
            // For now, we just print a message.
            System.out.println("KYC Verified for customer: " + customer.getFullName());

        } else {
            customer.setKycStatus(KycStatus.REJECTED);
            System.out.println("KYC Rejected for customer: " + customer.getFullName());
        }

        // Save the updated customer entity back to the database.
        customerRepository.save(customer);
    }

    // You can add other KYC-related methods here, like document submission.
}