package com.onboarding.controller;

import com.onboarding.dto.CustomerRegistrationRequest;
import com.onboarding.service.KycApplicationService;
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
    private final KycApplicationService kycApplicationService;

    public PublicUIController(KycApplicationService kycApplicationService) {
        this.kycApplicationService = kycApplicationService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new CustomerRegistrationRequest());
        return "ui/register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("registrationRequest") CustomerRegistrationRequest request, RedirectAttributes redirectAttributes) {
        try {
            kycApplicationService.submitNewApplication(request);
            redirectAttributes.addFlashAttribute("successMessage", "Your application has been submitted! You will be notified via email once it is reviewed.");
            return "redirect:/login";
        } catch (Exception e) {
            LOGGER.error("Registration failed for user: {}. Error: {}", request.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/ui/register";
        }
    }
}