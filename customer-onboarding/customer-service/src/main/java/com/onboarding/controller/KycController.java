package com.onboarding.controller;

import com.onboarding.service.KycService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/kyc") // This is the base path for all KYC-related APIs
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    /**
     * API endpoint for an admin to verify or reject a customer's KYC.
     * Accessible via: POST /api/kyc/{customerId}/verify
     * @param customerId The ID of the customer to verify.
     * @param request A JSON body, e.g., {"isVerified": true}
     * @return A success or error message.
     */
    @PostMapping("/{customerId}/verify")
    public ResponseEntity<String> verifyKyc(@PathVariable Long customerId, @RequestBody Map<String, Boolean> request) {
        try {
            // getOrDefault is a safe way to extract the boolean, defaulting to 'false' if the key is missing.
            boolean isVerified = request.getOrDefault("isVerified", false);
            
            kycService.verifyKyc(customerId, isVerified);
            
            String status = isVerified ? "VERIFIED" : "REJECTED";
            String responseMessage = "KYC status for customer " + customerId + " successfully updated to " + status;
            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing KYC: " + e.getMessage());
        }
    }
}