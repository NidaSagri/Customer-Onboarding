package com.onboarding.service;

import com.onboarding.exception.CustomerAlreadyExistsException;
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

import java.util.Optional;
import java.util.Set;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer registerCustomer(Customer customer) {
        customerRepository.findByPan(customer.getPan()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("A customer with PAN '" + customer.getPan() + "' already exists.");
        });
        customerRepository.findByAadhaar(customer.getAadhaar()).ifPresent(c -> {
            throw new CustomerAlreadyExistsException("A customer with Aadhaar '" + customer.getAadhaar() + "' already exists.");
        });
        userRepository.findByUsername(customer.getEmail()).ifPresent(u -> {
            throw new CustomerAlreadyExistsException("A user with email '" + customer.getEmail() + "' already exists.");
        });

        Customer savedCustomer = customerRepository.save(customer);

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
            .orElseThrow(() -> new RuntimeException("Error: Customer role not found."));

        User newUser = new User();
        newUser.setUsername(savedCustomer.getEmail());
        newUser.setPassword(passwordEncoder.encode("userpass"));
        newUser.setRoles(Set.of(customerRole));
        newUser.setCustomer(savedCustomer);

        userRepository.save(newUser);

        System.out.println("Created customer login: username='" + savedCustomer.getEmail() + "', password='userpass'");

        return savedCustomer;
    }

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    
    public Optional<Customer> findByPan(String pan) {
        return customerRepository.findByPan(pan);
    }
    
    public Page<Customer> findAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Page<Customer> searchCustomers(String keyword, Pageable pageable) {
        return customerRepository.searchCustomers(keyword, pageable);
    }

    public long countTotalCustomers() {
        return customerRepository.count();
    }

    public long countCustomersByKycStatus(KycStatus status) {
        return customerRepository.countByKycStatus(status);
    }
}