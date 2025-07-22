package com.onboarding.controller;

import com.onboarding.model.Account;
import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.model.Transaction;
import com.onboarding.repository.AccountRepository;
import com.onboarding.repository.CustomerRepository;
import com.onboarding.service.CustomerService;
import com.onboarding.service.KycService;
import com.onboarding.service.TransactionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CustomerService customerService;
    private final KycService kycService;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    public AdminController(CustomerService customerService, KycService kycService, AccountRepository accountRepository, TransactionService transactionService) {
        this.customerService = customerService;
        this.kycService = kycService;
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage;

        if (StringUtils.hasText(keyword)) {
            customerPage = customerService.searchCustomers(keyword, pageable);
            model.addAttribute("searchActive", true);
        } else {
            customerPage = customerService.findAllCustomers(pageable);
            model.addAttribute("searchActive", false);
        }

        model.addAttribute("customers", customerPage);
        model.addAttribute("keyword", keyword);
        
        model.addAttribute("totalCustomers", customerService.countTotalCustomers());
        model.addAttribute("pendingKyc", customerService.countCustomersByKycStatus(KycStatus.PENDING));
        model.addAttribute("verifiedAccounts", customerService.countCustomersByKycStatus(KycStatus.VERIFIED));

        return "admin/dashboard";
    }

    @GetMapping("/customer/{id}")
    public String customerDetails(@PathVariable Long id, Model model) {
        Customer customer = customerService.findCustomerById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
        
        Optional<Account> accountOpt = accountRepository.findByCustomer(customer);
        
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            // Use the corrected service method
            List<Transaction> transactions = transactionService.getTransactionHistory(account.getAccountNumber());
            model.addAttribute("transactions", transactions);
            model.addAttribute("account", account);
        }
        
        model.addAttribute("customer", customer);
        return "admin/customer-details";
    }

    @PostMapping("/customer/{id}/verify")
    public String verifyCustomerKyc(@PathVariable Long id, @RequestParam boolean approved, RedirectAttributes redirectAttributes) {
        try {
            kycService.verifyKyc(id, approved);
            String status = approved ? "approved" : "rejected";
            redirectAttributes.addFlashAttribute("message", "KYC for customer " + id + " has been " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing KYC: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}