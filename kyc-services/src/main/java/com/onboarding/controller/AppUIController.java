package com.onboarding.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppUIController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if (auth.getAuthority().equals("ROLE_ADMIN")) {
                    return "redirect:/admin/dashboard";
                }
                // Customer dashboard would be in another service, so we redirect to login
            }
        }
        return "redirect:/login";
    }
}