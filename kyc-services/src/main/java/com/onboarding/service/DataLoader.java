package com.onboarding.service;

import com.onboarding.model.Role;
import com.onboarding.model.User;
import com.onboarding.repository.RoleRepository;
import com.onboarding.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        LOGGER.info("Data loader is running...");
        
        // Create ROLE_ADMIN if it doesn't exist
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            LOGGER.info("ROLE_ADMIN not found, creating it.");
            Role newRole = new Role();
            newRole.setName("ROLE_ADMIN");
            return roleRepository.save(newRole);
        });

        // Create ROLE_CUSTOMER if it doesn't exist
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseGet(() -> {
            LOGGER.info("ROLE_CUSTOMER not found, creating it.");
            Role newRole = new Role();
            newRole.setName("ROLE_CUSTOMER");
            return roleRepository.save(newRole);
        });

        // Create a default admin user if one doesn't exist
        if (userRepository.findByUsername("admin").isEmpty()) {
            LOGGER.info("Admin user not found, creating a new one.");
            User admin = new User();
            admin.setUsername("admin");
            // IMPORTANT: Store the hashed password
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRoles(Set.of(adminRole));
            // Admin user does not have a customer profile, so customer field is null
            userRepository.save(admin);
            LOGGER.info("Successfully created admin user: username='admin', password='password'");
        } else {
            LOGGER.info("Admin user already exists. Skipping creation.");
        }
        
        LOGGER.info("Data loader finished.");
    }
}