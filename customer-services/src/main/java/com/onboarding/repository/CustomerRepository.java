package com.onboarding.repository;

import com.onboarding.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByEmail(String email);
  Optional<Customer> findByPan(String pan);
  Optional<Customer> findByAadhaar(String aadhaar);
}
