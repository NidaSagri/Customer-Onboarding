package com.onboarding;

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
    private final RoleRepository roleRepository;
    private final UserRepository userRepository; // Add UserRepository
    private final PasswordEncoder passwordEncoder; // Add PasswordEncoder

    public DataLoader(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        LOGGER.info("Customer-Service DataLoader is running...");
        
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_CUSTOMER");
        createRoleIfNotExists("ROLE_INTERNAL"); // *** NEW ROLE ***
        
        createInternalUser(); // *** NEW METHOD CALL ***

        LOGGER.info("Customer-Service DataLoader finished ensuring roles and internal user exist.");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            LOGGER.info("{} not found, creating it.", roleName);
            Role newRole = new Role();
            newRole.setName(roleName);
            roleRepository.save(newRole);
        }
    }
    
    // *** NEW METHOD TO CREATE THE INTERNAL USER ***
    private void createInternalUser() {
        if (userRepository.findByUsername("internal-user").isEmpty()) {
            LOGGER.info("Creating internal-user for inter-service communication.");
            Role internalRole = roleRepository.findByName("ROLE_INTERNAL")
                .orElseThrow(() -> new RuntimeException("CRITICAL: ROLE_INTERNAL not found!"));
                
            User internalUser = new User();
            internalUser.setUsername("internal-user");
            // Use a strong, non-guessable password in a real application
            internalUser.setPassword(passwordEncoder.encode("internal-password")); 
            internalUser.setRoles(Set.of(internalRole));
            // This user is not linked to a customer
            internalUser.setCustomer(null); 
            
            userRepository.save(internalUser);
        }
    }
}