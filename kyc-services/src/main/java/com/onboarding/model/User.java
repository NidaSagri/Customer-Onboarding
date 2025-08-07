package com.onboarding.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user account within the KYC service.
 * This entity is responsible for authentication and authorization. It holds the
 * user's login credentials and their assigned roles. After a user's KYC is
 * approved, this entity will also hold a reference to the ID of the permanent
 * customer record created in the separate customer-service.
 */
@Entity
@Table(name = "APP_USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // This should always store an encrypted password

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * This field holds the ID of the corresponding customer record that exists
     * in the separate `customer-service`. It is populated only after the KYC
     * application has been approved and the customer record has been created.
     * It is not a direct JPA relationship.
     */
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * Default constructor for JPA.
     */
    public User() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}