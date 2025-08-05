package com.onboarding.service;

import com.onboarding.dto.KycApplicationDataDTO;
import com.onboarding.exception.CustomerAlreadyExistsException;
import com.onboarding.model.Customer;
import com.onboarding.model.Role;
import com.onboarding.model.User;
import com.onboarding.repository.CustomerRepository;
import com.onboarding.repository.RoleRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public Customer createApprovedCustomer(KycApplicationDataDTO kycData) {
        // Validation to ensure we don't create duplicates
        if (customerRepository.findByPan(kycData.getPan()).isPresent() ||
            userRepository.findByUsername(kycData.getUsername()).isPresent() ||
            customerRepository.findByAadhaar(kycData.getAadhaar()).isPresent()) {
            throw new CustomerAlreadyExistsException("A customer or user with these unique details already exists.");
        }

        // 1. Create and save the Customer entity
        Customer customer = new Customer();
        customer.setFullName(kycData.getFullName());
        customer.setEmail(kycData.getEmail());
        customer.setPhone(kycData.getPhone());
        customer.setDob(kycData.getDob());
        customer.setAddress(kycData.getAddress());
        customer.setPan(kycData.getPan());
        customer.setAadhaar(kycData.getAadhaar());
        customer.setKycApplicationId(kycData.getKycApplicationId());
        Customer savedCustomer = customerRepository.save(customer);

        // 2. Create and save the associated User entity
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("CRITICAL: ROLE_CUSTOMER not found in the database."));
                
        User user = new User();
        user.setUsername(kycData.getUsername());
        // The password comes pre-encrypted from the kyc-service
        user.setPassword(kycData.getPassword());
        user.setRoles(Set.of(customerRole));
        user.setCustomer(savedCustomer);
        userRepository.save(user);

        return savedCustomer;
    }
}