package com.onboarding.controller;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.service.CustomerService;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ui")
public class PublicUIController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicUIController.class);
    private final CustomerService customerService;

    public PublicUIController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        LOGGER.info("Serving the registration page at /ui/register");
        model.addAttribute("registrationRequest", new CustomerRegistrationRequest());
        return "ui/register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("registrationRequest") CustomerRegistrationRequest request, RedirectAttributes redirectAttributes) {
        LOGGER.info("Processing new KYC application for user: {}", request.getUsername());
        try {
            // --- THE FIX: Call the new, correct service method ---
            customerService.registerKycApplication(request);
            
            // --- Update success message to reflect the new workflow ---
            redirectAttributes.addFlashAttribute("successMessage", "Your application has been submitted successfully! You will receive an email once your KYC is reviewed by the admin.");
            return "redirect:/login";
        } catch (ConstraintViolationException e) {
            String errorMessages = e.getConstraintViolations().stream()
                                     .map(cv -> cv.getMessage())
                                     .collect(Collectors.joining(", "));
            LOGGER.error("Application submission failed due to validation errors for user: {}. Errors: {}", request.getUsername(), errorMessages);
            redirectAttributes.addFlashAttribute("errorMessage", "Application failed: " + errorMessages);
            return "redirect:/ui/register";
        } catch (Exception e) {
            LOGGER.error("Application submission failed for user: {}. Error: {}", request.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Application failed: " + e.getMessage());
            return "redirect:/ui/register";
        }
    }
}