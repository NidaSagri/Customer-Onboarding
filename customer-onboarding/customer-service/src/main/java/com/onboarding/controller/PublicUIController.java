package com.onboarding.controller;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.service.CustomerService;
import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        LOGGER.info("Processing registration for user: {}", request.getUsername());
        try {
            customerService.registerCustomer(request);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in with your new credentials.");
            return "redirect:/login"; // This now works because AppUIController exists
        } catch (ConstraintViolationException e) {
            // Catch validation exceptions specifically to show a better message
            String errorMessages = e.getConstraintViolations().stream()
                                     .map(cv -> cv.getMessage())
                                     .collect(Collectors.joining(", "));
            LOGGER.error("Validation failed for user: {}. Errors: {}", request.getUsername(), errorMessages);
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + errorMessages);
            return "redirect:/ui/register";
        } catch (Exception e) {
            LOGGER.error("Registration failed for user: {}. Error: {}", request.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/ui/register";
        }
    }
}