package com.onboarding.service;

import com.onboarding.config.KafkaTopicConfig;
import com.onboarding.dto.KycStatusUpdateEvent;
import com.onboarding.dto.NewKycApplicationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final String ADMIN_EMAIL = "umeshsawant112233@gmail.com";
    private static final String SENDER_EMAIL = "umeshsawant112233@gmail.com";

    private final JavaMailSender javaMailSender;

    public EmailNotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_CUSTOMER_REGISTRATION, groupId = "kyc_group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeNewKycApplicationEvent(NewKycApplicationEvent event) {
        LOGGER.info("--> Received New KYC Application Event for ID: {}", event.getApplicationId());
        String subject = "New Customer Application Pending KYC Review";
        String adminDashboardLink = "http://localhost:8080/admin/kyc-application/" + event.getApplicationId();
        String body = String.format(
            "Hello Admin,\n\nA new customer application from %s (Application ID: %d) has been submitted and is awaiting KYC verification.\n\nPlease review the application here: %s\n\nThank you,\nOFSS Onboarding System",
            event.getApplicantName(), event.getApplicationId(), adminDashboardLink
        );
        sendEmail(ADMIN_EMAIL, subject, body);
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_KYC_STATUS_UPDATE, groupId = "kyc_group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeKycStatusUpdate(KycStatusUpdateEvent event) {
        LOGGER.info("--> Received KYC Status Update Event for: {}", event.getCustomerName());
        String subject;
        String body;
        String customerLoginLink = "http://localhost:8080/login";

        if ("VERIFIED".equals(event.getKycStatus())) {
            subject = "Congratulations! Your KYC with OFSS Bank has been Verified";
            body = String.format(
                "Hello %s,\n\nWe are pleased to inform you that your KYC verification is complete and has been approved.\n\n" +
                "Your Account Details:\n" +
                "  - Account Number: %s\n" +
                "  - Account Type: %s\n" +
                "  - IFSC Code: %s\n\n" +
                "Your account is now active. You can log in to your dashboard here:\n%s\n\n" +
                "Welcome to OFSS Bank!\n\n" +
                "Regards,\nThe Onboarding Team",
                event.getCustomerName(), event.getAccountNumber(), event.getAccountType(), event.getIfscCode(), customerLoginLink
            );
        } else {
            subject = "Important: Update on Your KYC Verification";
            String reasonText = (event.getRejectionReason() != null && !event.getRejectionReason().isBlank())
                    ? "The reason provided was: \"" + event.getRejectionReason() + "\""
                    : "This may be due to unclear documents or mismatched information.";
            body = String.format(
                "Hello %s,\n\nWe have reviewed your KYC submission and unfortunately, it could not be approved at this time.\n\n" +
                "%s\n\n" +
                "Please contact our support team for further assistance.\n\n" +
                "Regards,\nThe Onboarding Team",
                event.getCustomerName(), reasonText
            );
        }
        sendEmail(event.getCustomerEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage(); 
            message.setFrom(SENDER_EMAIL);
            message.setTo(to); 
            message.setSubject(subject); 
            message.setText(text);
            javaMailSender.send(message);
            LOGGER.info("Successfully sent email to {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send email to {}. Error: {}", to, e.getMessage());
        }
    }
}