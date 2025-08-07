package com.onboarding.repository;

import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycApplicationRepository extends JpaRepository<KycApplication, Long> {

    long countByKycStatus(KycStatus kycStatus);
    boolean existsByPan(String pan);
    boolean existsByAadhaar(String aadhaar);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<KycApplication> findTop5ByKycStatusOrderByIdDesc(KycStatus status);
    Page<KycApplication> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    // --- NEW, REQUIRED METHOD ---
    Optional<KycApplication> findByUsername(String username);
}