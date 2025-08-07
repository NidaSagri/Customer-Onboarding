package com.onboarding.service;

import com.onboarding.dto.CustomerDTO;
import com.onboarding.model.Customer;
import com.onboarding.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CustomerQueryService {

    private final CustomerRepository customerRepository;

    public CustomerQueryService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    public Optional<CustomerDTO> findCustomerById(Long id) {
        return customerRepository.findById(id).map(this::mapToDto);
    }
    
    public CustomerDTO mapToDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setFullName(customer.getFullName());
        dto.setFatherName(customer.getFatherName());
        dto.setMotherName(customer.getMotherName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setDob(customer.getDob());
        dto.setAddress(customer.getAddress());
        dto.setGender(customer.getGender());
        dto.setMaritalStatus(customer.getMaritalStatus());
        dto.setNationality(customer.getNationality());
        dto.setProfession(customer.getProfession());
        dto.setPan(customer.getPan());
        dto.setAadhaar(customer.getAadhaar());
        dto.setKycStatus(customer.getKycStatus().name());
        dto.setAadhaarPhotoBase64(customer.getAadhaarPhotoBase64());
        dto.setPanPhotoBase64(customer.getPanPhotoBase64());
        dto.setPassportPhotoBase64(customer.getPassportPhotoBase64());
        return dto;
    }
}