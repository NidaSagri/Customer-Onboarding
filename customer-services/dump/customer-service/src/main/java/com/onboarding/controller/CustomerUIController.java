package com.onboarding.controller;

import com.onboarding.dto.AccountCreationRequest;
import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.model.User;
import com.onboarding.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer") // This maps the whole controller to handle requests starting with /customer
public class CustomerUIController {

    private final UserRepository userRepository;
    private final AccountClient accountClient; // Use the Feign Client to talk to account-service

    public CustomerUIController(UserRepository userRepository, AccountClient accountClient) {
        this.userRepository = userRepository;
        this.accountClient = accountClient;
    }

    // Helper method to get the logged-in customer's details
    private Customer getAuthenticatedCustomer(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return user.getCustomer();
    }

    @GetMapping("/dashboard")
    public String customerDashboard(Model model, Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        model.addAttribute("customer", customer);
        model.addAttribute("kycStatus", customer.getKycStatus().name());

        // Call account-service to get account details
        try {
            AccountDTO account = accountClient.getAccountByCustomerId(customer.getId());
            model.addAttribute("account", account);
        } catch (Exception e) {
            // It's normal for a new user not to have an account, so we just don't add it to the model.
        }
        
        return "customer/dashboard";
    }

    @GetMapping("/create-account")
    public String showCreateAccountForm(Model model, Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        if (customer.getKycStatus() != KycStatus.VERIFIED) {
            return "redirect:/customer/dashboard";
        }
        
        AccountCreationRequest request = new AccountCreationRequest();
        request.setCustomerId(customer.getId());

        model.addAttribute("accountRequest", request);
        return "customer/create-account";
    }

    // This method is no longer used since registration creates the inactive account.
    // We will keep it here but it's part of the old flow. The UI now calls the Feign client.
    // A better approach would be to refactor AccountCreation into a single service call.
    // For now, this is kept for reference but is not hit by the new UI flow.
    // The account creation is now orchestrated by CustomerService -> AccountClient.

    // Note: The Transfer and Transactions pages have been removed as per your request to simplify.
    // If you need them back, we would add the methods here.
}