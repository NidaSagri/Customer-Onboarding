package com.onboarding.repository;

import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KycApplicationRepository extends JpaRepository<KycApplication, Long> {
    boolean existsByPan(String pan);
    boolean existsByAadhaar(String aadhaar);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<KycApplication> findByUsername(String username);
    long countByKycStatus(KycStatus status);
}