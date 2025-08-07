package com.onboarding.dto;

/**
 * Represents an event to be published to Kafka when a KYC application's status
 * is updated to either VERIFIED or REJECTED.
 * This event contains the necessary information for the notification service
 * to send an email to the customer.
 */
public class KycStatusUpdateEvent {

    private String customerName;
    private String customerEmail;
    private String kycStatus; // "VERIFIED" or "REJECTED"
    private String rejectionReason; // Only populated if the status is "REJECTED"

    /**
     * No-argument constructor required for JSON Deserialization (e.g., by Kafka listeners).
     */
    public KycStatusUpdateEvent() {
    }

    /**
     * A convenience constructor to create a fully initialized event object.
     *
     * @param customerName    The full name of the customer.
     * @param customerEmail   The email address of the customer for sending the notification.
     * @param kycStatus       The final status of the application ("VERIFIED" or "REJECTED").
     * @param rejectionReason The reason for rejection, if applicable. Can be null.
     */
    public KycStatusUpdateEvent(String customerName, String customerEmail, String kycStatus, String rejectionReason) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.kycStatus = kycStatus;
        this.rejectionReason = rejectionReason;
    }

    // --- Getters ---

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    // --- Setters ---

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}