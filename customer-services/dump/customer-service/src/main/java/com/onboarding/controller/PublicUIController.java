package com.onboarding.controller;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.service.CustomerService;
import jakarta.validation.ConstraintViolationException; // <-- IMPORT
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
        model.addAttribute("registrationRequest", new CustomerRegistrationRequest());
        return "ui/register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("registrationRequest") CustomerRegistrationRequest request, RedirectAttributes redirectAttributes) {
        try {
            customerService.registerKycApplication(request);
            redirectAttributes.addFlashAttribute("successMessage", "Your application has been submitted! You will be notified via email once it is reviewed.");
            return "redirect:/login";
        } catch (ConstraintViolationException e) {
            // This is the specific error for database validation (e.g., phone number format)
            String errorMessages = e.getConstraintViolations().stream()
                                     .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                                     .collect(Collectors.joining(", "));
            LOGGER.error("Validation failed for user: {}. Errors: {}", request.getUsername(), errorMessages);
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed due to invalid data: " + errorMessages);
            return "redirect:/ui/register";
        } catch (Exception e) {
            // This catches other errors, like "PAN already exists"
            LOGGER.error("Registration failed for user: {}. Error: {}", request.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/ui/register";
        }
    }
}