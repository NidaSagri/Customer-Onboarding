package com.onboarding.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppUIController {

    /**
     * Shows the custom login page.
     */
    @GetMapping("/login")
    public String login() {
        return "login"; // Renders login.html
    }

    /**
     * A smart dashboard that redirects users based on their role after login.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if (auth.getAuthority().equals("ROLE_ADMIN")) {
                    return "redirect:/admin/dashboard"; // Redirect admins to their dashboard
                }
                if (auth.getAuthority().equals("ROLE_CUSTOMER")) {
                    return "redirect:/customer/dashboard"; // Redirect customers to their dashboard
                }
            }
        }
        // Fallback to login if something is wrong
        return "redirect:/login";
    }
}