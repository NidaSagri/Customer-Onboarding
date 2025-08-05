package com.onboarding.repository;

import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycApplicationRepository extends JpaRepository<KycApplication, Long> {

    Optional<KycApplication> findByEmail(String email);
    Optional<KycApplication> findByPan(String pan);
    Optional<KycApplication> findByAadhaar(String aadhaar);
    Optional<KycApplication> findByUsername(String username);

    @Query("SELECT app FROM KycApplication app WHERE (app.username = :loginId OR app.email = :loginId) AND app.kycStatus = :status")
    Optional<KycApplication> findByUsernameOrEmailAndKycStatus(
        @Param("loginId") String loginIdentifier,
        @Param("status") KycStatus status
    );
}