package com.onboarding.service;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.dto.NewCustomerEvent;
import com.onboarding.exception.CustomerAlreadyExistsException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    // --- Repositories for both workflows ---
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KycApplicationRepository kycApplicationRepository; // <-- NEW

    // --- Components & Services ---
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    private final AccountClient accountClient;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository,
                           RoleRepository roleRepository, KycApplicationRepository kycApplicationRepository,
                           PasswordEncoder passwordEncoder, KafkaProducerService kafkaProducerService,
                           AccountClient accountClient) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.kycApplicationRepository = kycApplicationRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducerService = kafkaProducerService;
        this.accountClient = accountClient;
    }

    // ===================================================================================
    // NEW REGISTRATION WORKFLOW (STAGING)
    // ===================================================================================

    /**
     * Handles the first step of registration by creating a KycApplication record.
     * This does NOT create a final Customer or User.
     */
    @Transactional
    public KycApplication registerKycApplication(CustomerRegistrationRequest request) {
        LOGGER.info("Processing new KYC application for user: {}", request.getUsername());

        // Optional: Validate if a pending KycApplication already exists for these credentials
        kycApplicationRepository.findByUsername(request.getUsername()).ifPresent(app -> {
            throw new CustomerAlreadyExistsException("A KYC application with this username already exists.");
        });
        kycApplicationRepository.findByPan(request.getCustomer().getPan()).ifPresent(app -> {
            throw new CustomerAlreadyExistsException("A KYC application with this PAN already exists.");
        });
        kycApplicationRepository.findByAadhaar(request.getCustomer().getAadhaar()).ifPresent(app -> {
            throw new CustomerAlreadyExistsException("A KYC application with this Aadhaar already exists.");
        });

        KycApplication application = new KycApplication();
        
        // Copy data from the request to the staging entity
        application.setFullName(request.getCustomer().getFullName());
        application.setFatherName(request.getCustomer().getFatherName());
        application.setMotherName(request.getCustomer().getMotherName());
        application.setEmail(request.getCustomer().getEmail());
        application.setPhone(request.getCustomer().getPhone());
        application.setDob(request.getCustomer().getDob());
        application.setAddress(request.getCustomer().getAddress());
        application.setGender(request.getCustomer().getGender());
        application.setMaritalStatus(request.getCustomer().getMaritalStatus());
        application.setNationality(request.getCustomer().getNationality());
        application.setProfession(request.getCustomer().getProfession());
        application.setPan(request.getCustomer().getPan());
        application.setAadhaar(request.getCustomer().getAadhaar());
        application.setAadhaarPhotoBase64(request.getCustomer().getAadhaarPhotoBase64());
        application.setPanPhotoBase64(request.getCustomer().getPanPhotoBase64());
        application.setPassportPhotoBase64(request.getCustomer().getPassportPhotoBase64());
        application.setPreferredAccountType(request.getAccountType());
        application.setUsername(request.getUsername());
        application.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password

        KycApplication savedApplication = kycApplicationRepository.save(application);
        LOGGER.info("Saved new KYC application with ID: {}", savedApplication.getId());

        // Publish an event to notify admin of a new application needing review
        NewCustomerEvent event = new NewCustomerEvent(
            savedApplication.getId(),
            savedApplication.getFullName(),
            savedApplication.getEmail()
        );
        kafkaProducerService.sendNewCustomerNotification(event);

        return savedApplication;
    }

    // ===================================================================================
    // ADMIN-FACING WORKFLOW (FINALIZATION)
    // ===================================================================================
    
    /**
     * Approves a KYC application and creates the final Customer, User, and Account.
     * This method contains the logic from the original `registerCustomer` method.
     */
    @Transactional
    public Customer approveKycApplication(Long kycApplicationId) {
        LOGGER.info("Approving KYC application with ID: {}", kycApplicationId);
        KycApplication app = kycApplicationRepository.findById(kycApplicationId)
                .orElseThrow(() -> new RuntimeException("KYC Application not found with ID: " + kycApplicationId));

        // --- Validation against final tables ---
        userRepository.findByUsername(app.getUsername()).ifPresent(u -> {
            throw new CustomerAlreadyExistsException("A user with username '" + app.getUsername() + "' already exists.");
        });
        customerRepository.findByPan(app.getPan()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("A customer with PAN '" + app.getPan() + "' already exists.");
        });
        customerRepository.findByAadhaar(app.getAadhaar()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("A customer with Aadhaar '" + app.getAadhaar() + "' already exists.");
        });

        // --- Create final Customer entity from approved application ---
        Customer customer = new Customer();
        customer.setFullName(app.getFullName());
        customer.setFatherName(app.getFatherName());
        customer.setMotherName(app.getMotherName());
        customer.setEmail(app.getEmail());
        customer.setPhone(app.getPhone());
        customer.setDob(app.getDob());
        customer.setAddress(app.getAddress());
        customer.setPan(app.getPan());
        customer.setAadhaar(app.getAadhaar());
        customer.setAadhaarPhotoBase64(app.getAadhaarPhotoBase64());
        customer.setPanPhotoBase64(app.getPanPhotoBase64());
        customer.setPassportPhotoBase64(app.getPassportPhotoBase64());
        customer.setKycStatus(KycStatus.VERIFIED); // Set status to VERIFIED
        
        Customer savedCustomer = customerRepository.save(customer);

        // --- Create final User entity ---
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow(() -> new RuntimeException("Error: Customer role not found."));
        User newUser = new User();
        newUser.setUsername(app.getUsername());
        newUser.setPassword(app.getPassword()); // Use the already-encoded password
        newUser.setRoles(Set.of(customerRole));
        newUser.setCustomer(savedCustomer);
        userRepository.save(newUser);
        
        // --- Call Account Service to create an inactive account ---
        try {
            Map<String, Object> creationData = new HashMap<>();
            creationData.put("customerId", savedCustomer.getId());
            creationData.put("accountType", app.getPreferredAccountType());
            accountClient.createInactiveAccount(creationData);
        } catch (Exception e) {
            // In a real system, you'd handle this failure (e.g., compensating transaction)
            throw new RuntimeException("Failed to pre-create account. Registration rolled back. Error: " + e.getMessage());
        }

        // --- Update the original application status ---
        app.setKycStatus(KycStatus.VERIFIED);
        kycApplicationRepository.save(app);
        
        LOGGER.info("Successfully created Customer {} and User {} from application {}", savedCustomer.getId(), newUser.getUsername(), app.getId());

        return savedCustomer;
    }

    // ===================================================================================
    // EXISTING CUSTOMER MANAGEMENT (CRUD & QUERIES)
    // ===================================================================================
    
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

    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        User user = userRepository.findByCustomer(customer)
            .orElseThrow(() -> new RuntimeException("Could not find associated user for customer ID: " + customerId));
        
        userRepository.delete(user);
        // CascadeType.ALL on User's 'customer' field handles the Customer record deletion.
    }
    
    // --- Read operations for approved customers ---
    public Optional<Customer> findCustomerById(Long id) { return customerRepository.findById(id); }
    public Page<Customer> findAllCustomers(Pageable pageable) { return customerRepository.findAll(pageable); }
    public Page<Customer> searchCustomers(String keyword, Pageable pageable) { return customerRepository.searchCustomers(keyword, pageable); }
    public long countTotalCustomers() { return customerRepository.count(); }
    public long countCustomersByKycStatus(KycStatus status) { return customerRepository.countByKycStatus(status); }
    public List<Customer> findTop5CustomersByKycStatus(KycStatus status) {
        return customerRepository.findTop5ByKycStatusOrderByIdDesc(status);
    }
}