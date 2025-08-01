package com.onboarding.repository;

import com.onboarding.model.Customer;
import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycApplicationRepository extends JpaRepository<KycApplication, Long> {
    
   Optional<Customer> findByPan(String pan);

   
   Optional<Customer> findByAadhaar(String aadhaar);
   
   Optional<User> findByUsername(String username);
   
   long countByKycStatus(KycStatus kycStatus); 

}