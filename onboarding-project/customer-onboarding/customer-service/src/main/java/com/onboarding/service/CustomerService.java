package com.onboarding.service;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.dto.NewCustomerEvent;
import com.onboarding.exception.CustomerAlreadyExistsException;
import com.onboarding.feign.AccountClient; // <-- IMPORT FEIGN CLIENT
import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.model.Role;
import com.onboarding.model.User;
import com.onboarding.repository.CustomerRepository;
import com.onboarding.repository.RoleRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    private final AccountClient accountClient; // <-- INJECT FEIGN CLIENT

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, KafkaProducerService kafkaProducerService, AccountClient accountClient) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducerService = kafkaProducerService;
        this.accountClient = accountClient; // <-- INITIALIZE
    }
    
 // In CustomerService.java

    @Transactional
    public void deleteCustomer(Long customerId) {
        // Step 1: Find the Customer we want to delete.
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        // Step 2: Find the associated User. The User is the "owner" of the relationship.
        // We search by the customer object itself, which is the most reliable way.
        User user = userRepository.findByCustomer(customer)
            .orElseThrow(() -> new RuntimeException("Could not find associated user for customer ID: " + customerId));
        
        // Step 3: Delete the User. Because of CascadeType.ALL on the User's customer field,
        // the database will automatically delete the associated Customer record as well.
        userRepository.delete(user);
        
        // In a more advanced system, we would now publish a CustomerDeletedEvent to Kafka
        // so the account-service could clean up the orphaned account.
    }

    @Transactional
    public Customer registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = request.getCustomer();
        String username = request.getUsername();
        String password = request.getPassword();

        // --- Validation ---
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new CustomerAlreadyExistsException("A user with username '" + username + "' already exists.");
        });
        customerRepository.findByPan(customer.getPan()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("A customer with PAN '" + customer.getPan() + "' already exists.");
        });
        customerRepository.findByAadhaar(customer.getAadhaar()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("A customer with Aadhaar '" + customer.getAadhaar() + "' already exists.");
        });
        
        Customer savedCustomer = customerRepository.save(customer);

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow(() -> new RuntimeException("Error: Customer role not found."));
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRoles(Set.of(customerRole));
        newUser.setCustomer(savedCustomer);
        userRepository.save(newUser);
        
        // --- NEW: Call Account Service to create an inactive account ---
        try {
            Map<String, Object> creationData = new HashMap<>();
            creationData.put("customerId", savedCustomer.getId());
            creationData.put("accountType", request.getAccountType());
            accountClient.createInactiveAccount(creationData);
        } catch (Exception e) {
            // In a real system, you'd handle this failure (e.g., compensating transaction)
            throw new RuntimeException("Failed to pre-create account. Registration rolled back. Error: " + e.getMessage());
        }

        // Publish Kafka event to notify admin
        NewCustomerEvent event = new NewCustomerEvent(savedCustomer.getId(), savedCustomer.getFullName(), savedCustomer.getEmail());
        kafkaProducerService.sendNewCustomerNotification(event);

        return savedCustomer;
    }

    @Transactional
    public Customer updateCustomer(Long customerId, Customer customerDetails) {
        Customer existingCustomer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        existingCustomer.setFullName(customerDetails.getFullName());
        existingCustomer.setPhone(customerDetails.getPhone());
        existingCustomer.setAddress(customerDetails.getAddress());
        existingCustomer.setEmail(customerDetails.getEmail());
        existingCustomer.setDob(customerDetails.getDob());
        return customerRepository.save(existingCustomer);
    }

    // --- Read operations ---
    public Optional<Customer> findCustomerById(Long id) { return customerRepository.findById(id); }
    public Page<Customer> findAllCustomers(Pageable pageable) { return customerRepository.findAll(pageable); }
    public Page<Customer> searchCustomers(String keyword, Pageable pageable) { return customerRepository.searchCustomers(keyword, pageable); }
    public long countTotalCustomers() { return customerRepository.count(); }
    public long countCustomersByKycStatus(KycStatus status) { return customerRepository.countByKycStatus(status); }
    public List<Customer> findTop5CustomersByKycStatus(KycStatus status) {
        return customerRepository.findTop5ByKycStatusOrderByIdDesc(status);
    }
}