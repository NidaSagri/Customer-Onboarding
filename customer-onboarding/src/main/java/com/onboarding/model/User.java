package com.onboarding.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "app_user") // "user" is often a reserved keyword in SQL, so we use a different name
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // This will be the customer's email or a unique username

    @Column(nullable = false)
    private String password; // This will be a hashed password

    // Establishes the Many-to-Many relationship with Role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // This is the crucial link: A User entity is associated with one Customer entity.
    @OneToOne
    @JoinColumn(name = "customer_id", unique = true)
    private Customer customer;

    // --- Constructors ---
    public User() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}