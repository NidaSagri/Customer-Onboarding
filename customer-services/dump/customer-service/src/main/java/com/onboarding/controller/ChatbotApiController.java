package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.Customer;
import com.onboarding.model.KycApplication;
import com.onboarding.model.KycStatus;
import com.onboarding.model.User;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.CustomerQueryService;
import com.onboarding.service.KycApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotApiController {

    private final CustomerQueryService customerQueryService;
    private final KycApplicationService kycApplicationService;
    private final UserRepository userRepository;
    private final AccountClient accountClient;

    public ChatbotApiController(CustomerQueryService customerQueryService, KycApplicationService kycApplicationService, UserRepository userRepository, AccountClient accountClient) {
        this.customerQueryService = customerQueryService;
        this.kycApplicationService = kycApplicationService;
        this.userRepository = userRepository;
        this.accountClient = accountClient;
    }

    // --- ADMIN ENDPOINTS ---

    @GetMapping("/admin/list-by-kyc")
    public ResponseEntity<?> listApplicationsByKyc(@RequestParam String status) {
        KycStatus kycStatus;
        try {
            kycStatus = KycStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid KYC status provided.");
        }
        List<KycApplication> applications = kycApplicationService.findTop5ApplicationsByKycStatus(kycStatus);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/admin/search-customer")
    public ResponseEntity<?> searchCustomerForAdmin(@RequestParam String keyword) {
        Page<Customer> customerPage = customerQueryService.searchCustomers(keyword, PageRequest.of(0, 1));
        if (customerPage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Customer customer = customerPage.getContent().get(0);
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customer);
        try {
            AccountDTO account = accountClient.getAccountByCustomerId(customer.getId());
            response.put("account", account);
        } catch (Exception e) { /* Ignored */ }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalApplications", kycApplicationService.countTotalApplications());
        stats.put("pending", kycApplicationService.countPendingApplications());
        stats.put("verified", customerQueryService.countTotalCustomers());
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/admin/accounts-created-on-date")
    public ResponseEntity<List<AccountDTO>> getAccountsByDate(@RequestParam("date") String dateString) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateString);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<AccountDTO> accounts = accountClient.getAccountsCreatedBetween(startOfDay, endOfDay);
        return ResponseEntity.ok(accounts);
    }

    // --- CUSTOMER ENDPOINT ---

    @GetMapping("/customer/my-account")
    public ResponseEntity<?> getMyAccountDetails(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            
        Customer customer = user.getCustomer();
        
        // This handles the case where the user has logged in, but their KYC is still pending/rejected
        if (customer == null) {
            // --- THE FIX IS HERE ---
            // Use a standard if/else with the Optional for clarity and type safety
            Optional<KycApplication> applicationOpt = kycApplicationService.findApplicationByUsername(user.getUsername());
            
            if (applicationOpt.isPresent()) {
                // If the application is found, wrap it in a response
                return ResponseEntity.ok(applicationOpt.get());
            } else {
                // If no application is found either, then return a 404
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No customer profile or application found for this user.");
            }
        }

        // This part of the logic is for fully approved customers
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customer);
        try {
            AccountDTO account = accountClient.getAccountByCustomerId(customer.getId());
            response.put("account", account);
        } catch (Exception e) { /* Ignored */ }
        return ResponseEntity.ok(response);
    }
}