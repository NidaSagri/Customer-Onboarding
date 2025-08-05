package com.onboarding;

import com.onboarding.model.Role;
import com.onboarding.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);
    private final RoleRepository roleRepository;

    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        LOGGER.info("Customer-Service DataLoader is running...");
        
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_CUSTOMER");

        LOGGER.info("Customer-Service DataLoader finished ensuring roles exist.");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            LOGGER.info("{} not found, creating it.", roleName);
            Role newRole = new Role();
            newRole.setName(roleName);
            roleRepository.save(newRole);
        }
    }
}