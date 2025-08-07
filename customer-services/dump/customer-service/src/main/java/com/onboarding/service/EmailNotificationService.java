package com.onboarding.service;

import com.onboarding.config.KafkaTopicConfig;
import com.onboarding.dto.KycStatusUpdateEvent;
import com.onboarding.dto.NewCustomerEvent;
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
    private static final String SENDER_EMAIL = "umeshsawant112233@gmail.com"; // Should match spring.mail.username

    private final JavaMailSender javaMailSender;

    public EmailNotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Listens for new customer registration events from Kafka.
     * When an event is received, it constructs and sends an email notification to the admin.
     * @param event The NewCustomerEvent object deserialized from the Kafka message.
     */
    @KafkaListener(topics = KafkaTopicConfig.TOPIC_CUSTOMER_REGISTRATION, groupId = "onboarding_group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeNewCustomerEvent(NewCustomerEvent event) {
        LOGGER.info("--> Received New Customer Event for ID: {}", event.getCustomerId());

        String subject = "New Customer Registration Pending KYC";
        String adminDashboardLink = "http://localhost:8080/admin/customer/" + event.getCustomerId();
        String body = String.format(
            "Hello Admin,\n\nA new customer, %s (ID: %d), has registered and is awaiting KYC verification.\n\nPlease review their details by clicking the link below:\n%s\n\nThank you,\nOnboarding System",
            event.getCustomerName(),
            event.getCustomerId(),
            adminDashboardLink
        );

        try {
            sendEmail(ADMIN_EMAIL, subject, body);
            LOGGER.info("Successfully sent new customer notification email to admin for customer ID: {}", event.getCustomerId());
        } catch (Exception e) {
            LOGGER.error("Failed to send new customer notification email to admin for customer ID: {}. Error: {}", event.getCustomerId(), e.getMessage());
        }
    }

    /**
     * Listens for KYC status update events from Kafka.
     * When an event is received, it constructs and sends an email to the customer
     * informing them of their KYC status (Verified or Rejected).
     * @param event The KycStatusUpdateEvent object deserialized from the Kafka message.
     */
    @KafkaListener(topics = KafkaTopicConfig.TOPIC_KYC_STATUS_UPDATE, groupId = "onboarding_group", containerFactory = "kafkaListenerContainerFactory")
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
                "Your account is now active. You can log in to your dashboard to manage your account here:\n%s\n\n" +
                "Welcome to OFSS Bank!\n\n" +
                "Regards,\nThe Onboarding Team",
                event.getCustomerName(),
                event.getAccountNumber(),
                event.getAccountType(),
                event.getIfscCode(),
                customerLoginLink
            );
        } else { // REJECTED
            subject = "Important: Update on Your KYC Verification with OFSS Bank";
            
            // --- THE FIX ---
            // Use a more detailed email body that includes the reason.
            String reasonText = (event.getRejectionReason() != null && !event.getRejectionReason().isBlank())
                    ? "The reason provided was: \"" + event.getRejectionReason() + "\""
                    : "This may be due to unclear documents or mismatched information.";

            body = String.format(
                "Hello %s,\n\nWe have reviewed your KYC submission and unfortunately, it could not be approved at this time.\n\n" +
                "%s\n\n" +
                "Please contact our support team for further assistance. We apologize for any inconvenience.\n\n" +
                "Regards,\nThe Onboarding Team",
                event.getCustomerName(),
                reasonText
            );
        }
        
        try {
            sendEmail(event.getCustomerEmail(), subject, body);
            LOGGER.info("Successfully sent KYC status update email to customer: {}", event.getCustomerName());
        } catch (Exception e) {
            LOGGER.error("Failed to send KYC status update email to customer: {}. Error: {}", event.getCustomerName(), e.getMessage());
        }
    }

    /**
     * A helper method that uses Spring's JavaMailSender to send an email.
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param text The body content of the email.
     */
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom(SENDER_EMAIL);
        message.setTo(to); 
        message.setSubject(subject); 
        message.setText(text);
        javaMailSender.send(message);
    }
}