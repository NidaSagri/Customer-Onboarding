package com.onboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomerOnboardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerOnboardingApplication.class, args);
        System.out.println("\n\n--- Swagger UI is available at http://localhost:8080/swagger-ui.html ---\n\n");
    }
}