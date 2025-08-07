package com.onboarding.dto;

// Event to be published to Kafka when a new customer record is created
public class CustomerCreatedEvent {
    private Long customerId;
    private String customerName;
    private String customerEmail;

    public CustomerCreatedEvent() {}

    public CustomerCreatedEvent(Long customerId, String customerName, String customerEmail) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }

    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}