package com.onboarding.dto;

import com.onboarding.model.Customer;

public class CustomerRegistrationRequest {

    private Customer customer;
    private String username;
    private String password;
    private String accountType; // New field for account type

    // Getters and Setters
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}