package com.onboarding.controller;

import com.onboarding.service.KycService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/kyc") // This is your API endpoint base path
public class KycController {

    private final KycService kycService;

    // Use constructor injection here as well
    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    /**
     * API endpoint for an admin to verify or reject a customer's KYC.
     * Accessible via: POST /api/kyc/{customerId}/verify
     */
    @PostMapping("/{customerId}/verify")
    public ResponseEntity<String> verifyKyc(@PathVariable Long customerId, @RequestBody Map<String, Boolean> request) {
        // getOrDefault is a safe way to extract the boolean, defaulting to 'false' if the key is missing.
        boolean isVerified = request.getOrDefault("isVerified", false);
        
        kycService.verifyKyc(customerId, isVerified);
        
        String status = isVerified ? "VERIFIED" : "REJECTED";
        return ResponseEntity.ok("KYC status for customer " + customerId + " successfully updated to " + status);
    }
    
    // You can add other API endpoints here, like checking status
    // @GetMapping("/{customerId}/status")
    // ...
}