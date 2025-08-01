package com.onboarding.service;

import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.repository.KycApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class KycApplicationService {

    private final KycApplicationRepository kycApplicationRepository;

    public KycApplicationService(KycApplicationRepository kycApplicationRepository) {
        this.kycApplicationRepository = kycApplicationRepository;
    }

    public Page<KycApplication> findAllApplications(Pageable pageable) {
        return kycApplicationRepository.findAll(pageable);
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
}