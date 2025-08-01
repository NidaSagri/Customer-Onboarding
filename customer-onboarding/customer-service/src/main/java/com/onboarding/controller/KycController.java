package com.onboarding.controller;

import com.onboarding.service.KycService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/kyc/applications") // <-- Path updated for clarity
public class KycController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycController.class);
    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    /**
     * API endpoint for an admin to process (approve or reject) a KYC Application.
     * Accessible via: POST /api/kyc/applications/{applicationId}/process
     * @param applicationId The ID of the KYC Application to process.
     * @param request A JSON body that must contain "isApproved" (boolean) and
     *                can optionally contain "rejectionReason" (string) if rejecting.
     * @return A success or error message.
     */
    @PostMapping("/{applicationId}/process")
    public ResponseEntity<String> processKycApplication(
            @PathVariable Long applicationId,
            @RequestBody Map<String, Object> request) {
        
        LOGGER.info("API call received to process KYC application ID: {}", applicationId);
        
        try {
            if (!request.containsKey("isApproved") || !(request.get("isApproved") instanceof Boolean)) {
                return ResponseEntity.badRequest().body("Request body must contain a boolean 'isApproved' field.");
            }

            boolean isApproved = (Boolean) request.get("isApproved");
            
            String rejectionReason = null;
            if (!isApproved) {
                // If rejecting, the reason is mandatory from the API
                if (!request.containsKey("rejectionReason") || request.get("rejectionReason").toString().isBlank()) {
                     return ResponseEntity.badRequest().body("A 'rejectionReason' is mandatory when rejecting an application.");
                }
                rejectionReason = request.get("rejectionReason").toString();
            }
            
            // --- THE FIX: Call the new, correct service method ---
            kycService.processKycApplication(applicationId, isApproved, rejectionReason);
            
            String status = isApproved ? "APPROVED" : "REJECTED";
            String responseMessage = "KYC Application " + applicationId + " successfully processed with status: " + status;
            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            LOGGER.error("Error processing KYC application ID {}: {}", applicationId, e.getMessage());
            return ResponseEntity.badRequest().body("Error processing KYC application: " + e.getMessage());
        }
    }
}