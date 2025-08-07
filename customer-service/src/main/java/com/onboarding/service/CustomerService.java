package com.onboarding.service;

import com.onboarding.dto.FullRegistrationRequest;
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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
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
    private final KafkaProducerService kafkaProducerService; // Assuming you have this service

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository, RoleRepository roleRepository, NomineeRepository nomineeRepository, PasswordEncoder passwordEncoder, KafkaProducerService kafkaProducerService) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.nomineeRepository = nomineeRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Handles the complete, multi-page customer registration process.
     */
 // In CustomerService.java

    @Transactional
    public Customer createApprovedCustomer(KycApplicationDataDTO kycData) {
        // Validation remains the same...
        if (customerRepository.findByPan(kycData.getPan()).isPresent() ||
            userRepository.findByUsername(kycData.getUsername()).isPresent() ||
            customerRepository.findByAadhaar(kycData.getAadhaar()).isPresent()) {
            throw new CustomerAlreadyExistsException("A customer or user with these unique details already exists.");
        }

        Customer customer = new Customer();
        
        // *** THE FIX: Map ALL fields from the DTO ***
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
        
        // This was one of the missing fields
        customer.setRequestedAccountType(kycData.getRequestedAccountType());
        
        // These service preferences were also missing
        customer.setNetBankingEnabled(kycData.getNetBankingEnabled());
        customer.setDebitCardIssued(kycData.getDebitCardIssued());
        customer.setChequeBookIssued(kycData.getChequeBookIssued());
        
        // Set final status
        customer.setKycStatus(KycStatus.VERIFIED);
        customer.setPanLinked(true);
        customer.setAadhaarLinked(true);

        // ... Saving customer and creating user remains the same ...
        Customer savedCustomer = customerRepository.save(customer);
                
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
    private void validateUniqueness(FullRegistrationRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new CustomerAlreadyExistsException("Username '" + request.getUsername() + "' is already taken.");
        });
        customerRepository.findByEmail(request.getEmail()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("Email '" + request.getEmail() + "' is already registered.");
        });
        customerRepository.findByPan(request.getPan()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("PAN '" + request.getPan() + "' is already registered.");
        });
        customerRepository.findByAadhaar(request.getAadhaar()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("Aadhaar '" + request.getAadhaar() + "' is already registered.");
        });
        if (request.getNominee() != null && request.getNominee().getAadhaarNumber() != null) {
            nomineeRepository.findByAadhaarNumber(request.getNominee().getAadhaarNumber()).ifPresent(n -> {
                throw new CustomerAlreadyExistsException("A nominee with Aadhaar '" + n.getAadhaarNumber() + "' is already registered.");
            });
        }
    }
    
    private void mapRequestToCustomer(Customer customer, FullRegistrationRequest request) {
        customer.setFullname(request.getFullname());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDob(request.getDob());
        customer.setAddress(request.getAddress());
        customer.setGender(request.getGender());
        customer.setMaritalStatus(request.getMaritalStatus());
        customer.setFathersName(request.getFathersName());
        customer.setNationality(request.getNationality());
        customer.setProfession(request.getProfession());
        customer.setPan(request.getPan());
        customer.setAadhaar(request.getAadhaar());
        customer.setRequestedAccountType(request.getRequestedAccountType());
        customer.setNetBankingEnabled(request.isNetBankingEnabled());
        customer.setDebitCardIssued(request.isDebitCardIssued());
        customer.setChequeBookIssued(request.isChequeBookIssued());
        customer.setKycStatus(KycStatus.PENDING); // Default status
    }

    private void processDocuments(Customer customer, MultipartFile passportPhoto, MultipartFile panDoc, MultipartFile aadhaarDoc) throws IOException {
        if (passportPhoto != null && !passportPhoto.isEmpty()) {
            customer.setPassportPhotoBase64(Base64.getEncoder().encodeToString(passportPhoto.getBytes()));
            customer.setPassportPhotoContentType(passportPhoto.getContentType());
        }
        if (panDoc != null && !panDoc.isEmpty()) {
            customer.setPanPhotoBase64(Base64.getEncoder().encodeToString(panDoc.getBytes()));
            customer.setPanPhotoContentType(panDoc.getContentType());
        }
        if (aadhaarDoc != null && !aadhaarDoc.isEmpty()) {
            customer.setAadhaarPhotoBase64(Base64.getEncoder().encodeToString(aadhaarDoc.getBytes()));
            customer.setAadhaarPhotoContentType(aadhaarDoc.getContentType());
        }
    }
    
    private void handleNominee(Customer customer, NomineeDTO nomineeDTO) {
        Nominee nominee = new Nominee();
        nominee.setName(nomineeDTO.getName());
        nominee.setMobile(nomineeDTO.getMobile());
        nominee.setAddress(nomineeDTO.getAddress());
        nominee.setAadhaarNumber(nomineeDTO.getAadhaarNumber());
        customer.setNominee(nominee); // This uses the helper method to set the bidirectional link
    }

    private void createUserForCustomer(Customer customer, String username, String password) {
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("CRITICAL: ROLE_CUSTOMER not found in the database."));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(customerRole));
        user.setCustomer(customer);
        userRepository.save(user);
    }

    // --- EXISTING METHODS REQUIRED BY ADMIN/UI CONTROLLERS ---

    public Page<Customer> findAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Page<Customer> searchCustomers(String keyword, Pageable pageable) {
        // Use the new, more powerful search method
        return customerRepository.searchByKeyword(keyword, pageable);
    }

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public long countTotalCustomers() {
        return customerRepository.count();
    }

    public long countCustomersByKycStatus(KycStatus status) {
        return customerRepository.countByKycStatus(status);
    }
    
    public List<Customer> findTop5CustomersByKycStatus(KycStatus status) {
        return customerRepository.findTop5ByKycStatusOrderByIdDesc(status);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        // Finding the user by customer ID is safer.
        User user = userRepository.findByCustomerId(id)
            .orElseThrow(() -> new RuntimeException("User not found for customer ID: " + id));
        userRepository.delete(user);
        // Deleting the user should cascade-delete the customer if the relationship is owned by User.
        // If not, we delete the customer explicitly. A double-check is fine.
        customerRepository.findById(id).ifPresent(customerRepository::delete);
    }

    @Transactional
    public Customer updateKycStatus(Long customerId, String status, String reason) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        KycStatus newStatus = KycStatus.valueOf(status.toUpperCase());

        if (customer.getKycStatus() != KycStatus.PENDING) {
            throw new IllegalStateException("KYC for this customer has already been processed.");
        }

        customer.setKycStatus(newStatus);
        
        // If verified, we can set the linked flags
        if (newStatus == KycStatus.VERIFIED) {
            customer.setPanLinked(true);
            customer.setAadhaarLinked(true);
        }
        
        Customer updatedCustomer = customerRepository.save(customer);

        // Here we would create and send a Kafka event.
        // For now, the logic is just to update the status. The kyc-service will send the email.
        
        return updatedCustomer;
    }
    

    @Transactional
    public Customer updateCustomer(long id, Customer customerFromForm) {
        // Step 1: FETCH the original, complete customer from the database.
        Customer existingCustomer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        // Step 2: UPDATE the original entity by selectively copying fields from the form object.
        // This prevents accidentally nullifying fields that weren't on the form (like PAN, Aadhaar, etc.).
        
        // --- Fields that an Admin can edit ---
        existingCustomer.setFullname(customerFromForm.getFullname());
        existingCustomer.setEmail(customerFromForm.getEmail());
        existingCustomer.setPhone(customerFromForm.getPhone());
        existingCustomer.setDob(customerFromForm.getDob());
        existingCustomer.setAddress(customerFromForm.getAddress());
        existingCustomer.setMaritalStatus(customerFromForm.getMaritalStatus());
        existingCustomer.setProfession(customerFromForm.getProfession());
        existingCustomer.setFathersName(customerFromForm.getFathersName());
        
        // IMPORTANT: We do NOT update immutable or system-managed fields like:
        // - existingCustomer.setPan(...)
        // - existingCustomer.setAadhaar(...)
        // - existingCustomer.setKycStatus(...)

        // Step 3: SAVE the updated original entity.
        return customerRepository.save(existingCustomer);
    }
}