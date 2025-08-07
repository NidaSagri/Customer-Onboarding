package com.onboarding.security;

import com.onboarding.model.KycApplication;
import com.onboarding.repository.KycApplicationRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final KycApplicationRepository kycApplicationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDetailsServiceImpl(KycApplicationRepository kycApplicationRepository, PasswordEncoder passwordEncoder) {
        this.kycApplicationRepository = kycApplicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

 // In UserDetailsServiceImpl.java

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // --- Hardcoded admin user for the UI ---
        if ("admin".equalsIgnoreCase(usernameOrEmail)) {
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            return new org.springframework.security.core.userdetails.User(
                "admin",
                passwordEncoder.encode("password"), 
                authorities
            );
        }

        // *** THE FIX FOR CUSTOMER LOGIN ***
        // We now pass the login identifier to both parameters of the new repository method.
        KycApplication application = kycApplicationRepository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail) // <--- CORRECTED CALL
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password: " + usernameOrEmail));

        // Grant them the customer role so they can access their dashboard
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

        return new org.springframework.security.core.userdetails.User(
            application.getUsername(),
            application.getPassword(), // Password is ALREADY encrypted in the database
            authorities
        );
    }
}