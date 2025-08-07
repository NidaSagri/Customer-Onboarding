package com.onboarding.controller;

import com.onboarding.service.KycService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/kyc/applications")
public class KycController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycController.class);
    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping("/{applicationId}/process")
    public ResponseEntity<String> processKycApplication(
            @PathVariable Long applicationId,
            @RequestBody Map<String, Object> request) {
        
        LOGGER.info("API call received to process KYC application ID: {}", applicationId);
        
        try {
            boolean isApproved = (Boolean) request.get("isApproved");
            String rejectionReason = request.getOrDefault("rejectionReason", "").toString();
            
            kycService.processKycApplication(applicationId, isApproved, rejectionReason);
            
            String status = isApproved ? "APPROVED" : "REJECTED";
            return ResponseEntity.ok("KYC Application " + applicationId + " successfully processed with status: " + status);
        } catch (Exception e) {
            LOGGER.error("Error processing KYC application ID {}: {}", applicationId, e.getMessage());
            return ResponseEntity.badRequest().body("Error processing KYC application: " + e.getMessage());
        }
    }
}