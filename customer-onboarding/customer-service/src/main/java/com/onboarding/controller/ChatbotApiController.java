package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.model.User;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotApiController {

    private final CustomerService customerService;
    private final UserRepository userRepository;
    private final AccountClient accountClient;

    public ChatbotApiController(CustomerService customerService, UserRepository userRepository, AccountClient accountClient) {
        this.customerService = customerService;
        this.userRepository = userRepository;
        this.accountClient = accountClient;
    }

    // --- NEW, REQUIRED ENDPOINT for listing customers by KYC status ---
    @GetMapping("/admin/list-by-kyc")
    public ResponseEntity<?> listCustomersByKyc(@RequestParam String status) {
        KycStatus kycStatus;
        try {
            kycStatus = KycStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid KYC status provided. Use PENDING, VERIFIED, or REJECTED.");
        }
        // Assumes a method findTop5CustomersByKycStatus exists in your CustomerService
        List<Customer> customers = customerService.findTop5CustomersByKycStatus(kycStatus);
        return ResponseEntity.ok(customers);
    }

    // --- Endpoint for admin to get full customer details ---
    @GetMapping("/admin/search-customer")
    public ResponseEntity<?> searchCustomerForAdmin(@RequestParam String keyword) {
        Page<Customer> customerPage = customerService.searchCustomers(keyword, PageRequest.of(0, 1));
        if (customerPage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Customer customer = customerPage.getContent().get(0);
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customer);
        try {
            AccountDTO account = accountClient.getAccountByCustomerId(customer.getId());
            response.put("account", account);
        } catch (Exception e) {
            // No account found is a valid state
        }
        return ResponseEntity.ok(response);
    }

    // --- Endpoint for getting dashboard stats ---
    @GetMapping("/admin/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", customerService.countTotalCustomers());
        stats.put("pending", customerService.countCustomersByKycStatus(KycStatus.PENDING));
        stats.put("verified", customerService.countCustomersByKycStatus(KycStatus.VERIFIED));
        stats.put("rejected", customerService.countCustomersByKycStatus(KycStatus.REJECTED));
        return ResponseEntity.ok(stats);
    }
    
    // --- Endpoint for logged-in customer's own data ---
    @GetMapping("/customer/my-account")
    public ResponseEntity<?> getMyAccountDetails(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        Customer customer = user.getCustomer();
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No customer profile linked to this user.");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customer);
        try {
            AccountDTO account = accountClient.getAccountByCustomerId(customer.getId());
            response.put("account", account);
        } catch (Exception e) {
            // No account found is a valid state
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/accounts-created-on-date")
    public ResponseEntity<List<AccountDTO>> getAccountsByDate(@RequestParam("date") String dateString) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateString); // Expects "YYYY-MM-DD" format
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        List<AccountDTO> accounts = accountClient.getAccountsCreatedBetween(startOfDay, endOfDay);
        return ResponseEntity.ok(accounts);
    }
}