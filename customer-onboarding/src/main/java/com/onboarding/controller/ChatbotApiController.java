package com.onboarding.controller;

import com.onboarding.model.*;
import com.onboarding.repository.AccountRepository;
import com.onboarding.repository.CustomerRepository;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.AccountService;
import com.onboarding.service.CustomerService;
import com.onboarding.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotApiController {

    // Dependencies (unchanged)
    private final CustomerService customerService;
    private final AccountService accountService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TransactionService transactionService;

    public ChatbotApiController(CustomerService customerService, AccountService accountService, UserRepository userRepository, CustomerRepository customerRepository, TransactionService transactionService) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.transactionService = transactionService;
    }

    // --- FINAL CUSTOMER ENDPOINT: Returns ALL data for the logged-in user ---
    @GetMapping("/customer/my-account")
    public ResponseEntity<?> getMyAccountDetails(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Customer customer = user.getCustomer();
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No customer profile linked to this user.");
        }

        // Bundle all data for the response
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customer); // Personal, Identity, KYC info

        Optional<Account> accountOpt = accountService.findAccountByCustomer(customer);
        accountOpt.ifPresent(account -> {
            response.put("account", account); // Account Type, Number, Balance
            List<Transaction> transactions = transactionService.getTransactionHistory(account.getAccountNumber());
            response.put("transactions", transactions); // Full Transaction History
        });

        return ResponseEntity.ok(response);
    }

    // --- FINAL ADMIN ENDPOINT: Returns ALL data for a searched customer ---
    @GetMapping("/admin/search-customer")
    public ResponseEntity<?> searchCustomerForAdmin(@RequestParam String keyword) {
        Page<Customer> customerPage = customerService.searchCustomers(keyword, PageRequest.of(0, 1));
        if (customerPage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Customer customer = customerPage.getContent().get(0);
        
        // Bundle all data for the response
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customer);

        Optional<Account> accountOpt = accountService.findAccountByCustomer(customer);
        accountOpt.ifPresent(account -> {
            response.put("account", account);
            List<Transaction> transactions = transactionService.getTransactionHistory(account.getAccountNumber());
            response.put("transactions", transactions);
        });
        
        return ResponseEntity.ok(response);
    }

    // Admin dashboard stats tool remains useful and unchanged
    @GetMapping("/admin/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", customerService.countTotalCustomers());
        stats.put("pending", customerService.countCustomersByKycStatus(KycStatus.PENDING));
        stats.put("verified", customerService.countCustomersByKycStatus(KycStatus.VERIFIED));
        stats.put("rejected", customerService.countCustomersByKycStatus(KycStatus.REJECTED));
        return ResponseEntity.ok(stats);
    }
}