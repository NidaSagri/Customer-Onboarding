package com.onboarding;

import com.onboarding.model.Account;
import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.model.Role;
import com.onboarding.model.User;
import com.onboarding.repository.CustomerRepository;
import com.onboarding.repository.RoleRepository;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.AccountService; // <-- IMPORT
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final AccountService accountService; // <-- INJECT

    public DataLoader(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.accountService = accountService; // <-- INITIALIZE
    }

    @Override
    @Transactional // Use transactional to ensure all operations succeed or fail together
    public void run(String... args) throws Exception {
        // 1. Create Roles
        Role adminRole = createRoleIfNotExists("ROLE_ADMIN");
        Role customerRole = createRoleIfNotExists("ROLE_CUSTOMER");

        // 2. Create Admin User
        createAdminUser(adminRole);

        // 3. Create a sample, KYC-verified customer with a bank account
        createSampleCustomer(customerRole);
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName);
            return roleRepository.save(newRole);
        });
    }

    private void createAdminUser(Role adminRole) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            System.out.println("Created admin user: username='admin', password='password'");
        }
    }

    private void createSampleCustomer(Role customerRole) {
        // Check if the sample customer already exists to prevent duplicates on restart
        if (userRepository.findByUsername("jane.doe@example.com").isEmpty()) {
            // Step 1: Create the Customer entity
            Customer customer = new Customer();
            customer.setFullName("Jane Doe");
            customer.setEmail("jane.doe@example.com");
            customer.setPhone("9876543210");
            customer.setDob(LocalDate.of(1990, 5, 15));
            customer.setAddress("123 Sample St, Example City");
            customer.setPan("ABCDE1234F");
            customer.setAadhaar("123456789012");
            customer.setKycStatus(KycStatus.VERIFIED); // <-- PRE-VERIFIED
            Customer savedCustomer = customerRepository.save(customer);

            // Step 2: Create the associated User login
            User customerUser = new User();
            customerUser.setUsername(savedCustomer.getEmail());
            customerUser.setPassword(passwordEncoder.encode("userpass")); // Set a default password
            customerUser.setRoles(Set.of(customerRole));
            customerUser.setCustomer(savedCustomer);
            userRepository.save(customerUser);

            // Step 3: Create a bank account for the customer
            try {
                // Initial deposit of 5000 for demonstration
                Account account = accountService.createAccount(savedCustomer.getId(), "SAVINGS", new BigDecimal("5000.00"));
                System.out.println("Created sample customer: username='jane.doe@example.com', password='userpass'");
                System.out.println("Created account " + account.getAccountNumber() + " for Jane Doe with balance " + account.getBalance());
            } catch (Exception e) {
                System.err.println("Failed to create account for sample customer: " + e.getMessage());
            }
        }
    }
}