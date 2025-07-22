package com.onboarding.controller;

import com.onboarding.model.Customer;
import com.onboarding.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui") // Mapped to /ui to handle public UI routes
public class PublicUIController {

    private final CustomerService customerService;

    public PublicUIController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Displays the customer registration form.
     * Accessible at GET /ui/register
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "ui/register"; // Renders src/main/resources/templates/ui/register.html
    }

    /**
     * Processes the customer registration form submission.
     * Accessible at POST /ui/register
     */
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("customer") Customer customer, RedirectAttributes redirectAttributes) {
        try {
            customerService.registerCustomer(customer);
            // On success, redirect to the login page with a success message
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            // On failure, redirect back to the registration form with an error
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/ui/register";
        }
    }
}