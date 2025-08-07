package com.onboarding.service;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(CustomerDTO dto) {
        LOGGER.info("Attempting to create a new customer record for PAN: {}", dto.getPan());

        // Business validation
        if (customerRepository.findByPan(dto.getPan()).isPresent()) {
            LOGGER.error("Customer creation failed. PAN {} already exists.", dto.getPan());
            throw new IllegalStateException("Customer with this PAN already exists.");
        }
        if (customerRepository.findByAadhaar(dto.getAadhaar()).isPresent()) {
            LOGGER.error("Customer creation failed. Aadhaar {} already exists.", dto.getAadhaar());
            throw new IllegalStateException("Customer with this Aadhaar already exists.");
        }

        Customer customer = new Customer();
        customer.setFullName(dto.getFullName());
        customer.setFatherName(dto.getFatherName());
        customer.setMotherName(dto.getMotherName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setDob(dto.getDob());
        customer.setAddress(dto.getAddress());
        customer.setGender(dto.getGender());
        customer.setMaritalStatus(dto.getMaritalStatus());
        customer.setNationality(dto.getNationality());
        customer.setProfession(dto.getProfession());
        customer.setPan(dto.getPan());
        customer.setAadhaar(dto.getAadhaar());
        customer.setKycStatus(KycStatus.valueOf(dto.getKycStatus()));
        customer.setAadhaarPhotoBase64(dto.getAadhaarPhotoBase64());
        customer.setPanPhotoBase64(dto.getPanPhotoBase64());
        customer.setPassportPhotoBase64(dto.getPassportPhotoBase64());
        
        Customer savedCustomer = customerRepository.save(customer);
        LOGGER.info("Successfully created and saved new customer with ID: {}", savedCustomer.getId());
        
        // Here you could publish a CustomerCreatedEvent to Kafka if needed
        
        return savedCustomer;
    }
}