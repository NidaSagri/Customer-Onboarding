package com.onboarding.service;

import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.repository.KycApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // This service is for reading data only
public class KycApplicationService {

    private final KycApplicationRepository kycApplicationRepository;

    public KycApplicationService(KycApplicationRepository kycApplicationRepository) {
        this.kycApplicationRepository = kycApplicationRepository;
    }

    public Page<KycApplication> findAllApplications(Pageable pageable) {
        return kycApplicationRepository.findAll(pageable);
    }
    
    // Search for applications by name for the admin dashboard
    public Page<KycApplication> searchApplications(String keyword, Pageable pageable) {
        return kycApplicationRepository.findByFullNameContainingIgnoreCase(keyword, pageable);
    }

    public Optional<KycApplication> findApplicationById(Long id) {
        return kycApplicationRepository.findById(id);
    }

    public long countTotalApplications() {
        return kycApplicationRepository.count();
    }

    public long countPendingApplications() {
        return kycApplicationRepository.countByKycStatus(KycStatus.PENDING);
    }

    // New method for the chatbot
    public List<KycApplication> findTop5ApplicationsByKycStatus(KycStatus status) {
        return kycApplicationRepository.findTop5ByKycStatusOrderByIdDesc(status);
    }
    
 // --- NEW, REQUIRED METHOD ---
    public Optional<KycApplication> findApplicationByUsername(String username) {
        return kycApplicationRepository.findByUsername(username);
    }
}