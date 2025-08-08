package com.onboarding.service;

import com.onboarding.dto.KycApplicationDataDTO;
import com.onboarding.dto.NomineeDTO;
import com.onboarding.exception.CustomerAlreadyExistsException;
import com.onboarding.model.*;
import com.onboarding.repository.CustomerRepository;
import com.onboarding.repository.NomineeRepository;
import com.onboarding.repository.RoleRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NomineeRepository nomineeRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository, RoleRepository roleRepository, NomineeRepository nomineeRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.nomineeRepository = nomineeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer createApprovedCustomer(KycApplicationDataDTO kycData) {
        // Validation for uniqueness
        if (customerRepository.findByPan(kycData.getPan()).isPresent() ||
            userRepository.findByUsername(kycData.getUsername()).isPresent() ||
            customerRepository.findByAadhaar(kycData.getAadhaar()).isPresent()) {
            throw new CustomerAlreadyExistsException("A customer or user with these unique details already exists.");
        }
        
        if (kycData.getNominee() != null) {
            nomineeRepository.findByAadhaarNumber(kycData.getNominee().getAadhaarNumber()).ifPresent(n -> {
                 throw new CustomerAlreadyExistsException("A nominee with that Aadhaar number is already registered.");
            });
        }

        Customer customer = new Customer();
        
        // --- Map ALL fields from the DTO to the Customer entity ---
        customer.setFullname(kycData.getFullName());
        customer.setDob(kycData.getDob());
        customer.setGender(kycData.getGender());
        customer.setMaritalStatus(kycData.getMaritalStatus());
        customer.setFathersName(kycData.getFathersName());
        customer.setNationality(kycData.getNationality());
        customer.setProfession(kycData.getProfession());
        customer.setAddress(kycData.getAddress());
        customer.setEmail(kycData.getEmail());
        customer.setPhone(kycData.getPhone());
        customer.setPan(kycData.getPan());
        customer.setAadhaar(kycData.getAadhaar());
        customer.setRequestedAccountType(kycData.getRequestedAccountType());
        customer.setNetBankingEnabled(kycData.getNetBankingEnabled());
        customer.setDebitCardIssued(kycData.getDebitCardIssued());
        customer.setChequeBookIssued(kycData.getChequeBookIssued());
        customer.setPassportPhotoBase64(kycData.getPassportPhotoBase64());
        customer.setPanPhotoBase64(kycData.getPanPhotoBase64());
        customer.setPanPhotoContentType(kycData.getPanPhotoContentType());
        customer.setAadhaarPhotoBase64(kycData.getAadhaarPhotoBase64());
        customer.setAadhaarPhotoContentType(kycData.getAadhaarPhotoContentType());
        
        customer.setKycStatus(KycStatus.VERIFIED);
        customer.setPanLinked(true);
        customer.setAadhaarLinked(true);

        // *** THE FIX: Handle the nominee if it exists in the DTO ***
        if (kycData.getNominee() != null) {
            Nominee nominee = new Nominee();
            NomineeDTO nomineeDTO = kycData.getNominee();
            nominee.setName(nomineeDTO.getName());
            nominee.setMobile(nomineeDTO.getMobile());
            nominee.setAddress(nomineeDTO.getAddress());
            nominee.setAadhaarNumber(nomineeDTO.getAadhaarNumber());
            customer.setNominee(nominee); // This links the two entities
        }

        Customer savedCustomer = customerRepository.save(customer);

        // Create the associated User entity for login
        User user = new User();
        user.setUsername(kycData.getUsername());
        user.setPassword(kycData.getPassword());
        
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("CRITICAL: ROLE_CUSTOMER not found."));
        user.setRoles(Set.of(customerRole));
        user.setCustomer(savedCustomer);
        userRepository.save(user);

        return savedCustomer;
    }
    
    // --- Other service methods (unchanged) ---
    public Page<Customer> findAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }
    public Page<Customer> searchCustomers(String keyword, Pageable pageable) {
        return customerRepository.searchByKeyword(keyword, pageable);
    }
    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }
}