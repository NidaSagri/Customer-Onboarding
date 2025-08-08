package com.onboarding.controller;

import com.onboarding.model.KycApplication;
import com.onboarding.repository.KycApplicationRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
public class DocumentController {

    private final KycApplicationRepository kycApplicationRepository;

    public DocumentController(KycApplicationRepository kycApplicationRepository) {
        this.kycApplicationRepository = kycApplicationRepository;
    }

    @GetMapping("/{id}/document/{type}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id, @PathVariable String type) {
        Optional<KycApplication> appOptional = kycApplicationRepository.findById(id);
        if (appOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        KycApplication app = appOptional.get();
        String base64DataUri;
        
        switch (type.toLowerCase()) {
            case "passport":
                base64DataUri = app.getPassportPhotoBase64();
                break;
            case "pan":
                base64DataUri = app.getPanPhotoBase64();
                break;
            case "aadhaar":
                base64DataUri = app.getAadhaarPhotoBase64();
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        if (base64DataUri == null || base64DataUri.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // A Base64 data URI looks like "data:image/png;base64,iVBORw0KGgo..."
            // We need to split it at the comma to get the actual data.
            String[] parts = base64DataUri.split(",");
            if (parts.length != 2) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String mimeTypePart = parts[0]; // e.g., "data:application/pdf;base64"
            String encodedData = parts[1];

            // Extract the MIME type (e.g., "application/pdf")
            String mimeType = mimeTypePart.split(":")[1].split(";")[0];

            byte[] decodedBytes = Base64.getDecoder().decode(encodedData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            // This header suggests the browser should display the file inline
            headers.add("Content-Disposition", "inline; filename=\"" + type + "\""); 

            return new ResponseEntity<>(decodedBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}