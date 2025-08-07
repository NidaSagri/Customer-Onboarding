package com.onboarding.dto;

/**
 * Represents an event that is published when a new KYC (Know Your Customer)
 * application has been successfully submitted by a user.
 * This event is typically sent to a message queue (like Kafka) to notify
 * other parts of the system, such as an admin notification service,
 * that a new application is pending review.
 */
public class NewKycApplicationEvent {

    private Long applicationId;
    private String customerName;
    private String customerEmail;

    /**
     * No-argument constructor.
     * Required for frameworks like Jackson (used by Spring Kafka) for deserialization
     * from JSON or other formats into a Java object.
     */
    public NewKycApplicationEvent() {
    }

    /**
     * A convenience constructor to easily create an instance of the event
     * with all necessary data.
     *
     * @param applicationId The unique identifier of the newly created KYC application.
     * @param customerName  The full name of the applicant.
     * @param customerEmail The email address of the applicant.
     */
    public NewKycApplicationEvent(Long applicationId, String customerName, String customerEmail) {
        this.applicationId = applicationId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }

    // --- Getters ---

    public Long getApplicationId() {
        return applicationId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    // --- Setters ---

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}