package com.onboarding.service;

import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // This service is for reading data only
public class CustomerQueryService {

    private final CustomerRepository customerRepository;

    public CustomerQueryService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // --- All the read operations now live here ---

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Page<Customer> findAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Page<Customer> searchCustomers(String keyword, Pageable pageable) {
        // You will need to ensure the searchCustomers method exists in CustomerRepository
        return customerRepository.searchCustomers(keyword, pageable);
    }

    public long countTotalCustomers() {
        return customerRepository.count();
    }

    public long countCustomersByKycStatus(KycStatus status) {
        // This will now specifically count VERIFIED customers
        return customerRepository.countByKycStatus(status);
    }
}