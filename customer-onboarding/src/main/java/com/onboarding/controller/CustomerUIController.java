package com.onboarding.controller;

import com.onboarding.dto.TransactionRequestDTO;
import com.onboarding.model.Account;
import com.onboarding.model.Customer;
import com.onboarding.model.Transaction;
import com.onboarding.model.User;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.AccountService;
import com.onboarding.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerUIController {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public CustomerUIController(UserRepository userRepository, AccountService accountService, TransactionService transactionService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

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
        accountService.findAccountByCustomer(customer).ifPresent(account -> model.addAttribute("account", account));
        return "customer/dashboard";
    }

    @PostMapping("/create-account")
    public String createAccount(Authentication authentication, RedirectAttributes redirectAttributes) {
        Customer customer = getAuthenticatedCustomer(authentication);
        try {
            // Create account with a default zero initial deposit from the UI button.
            accountService.createAccount(customer.getId(), "SAVINGS", BigDecimal.ZERO);
            redirectAttributes.addFlashAttribute("successMessage", "Your account has been created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/customer/dashboard";
    }
    
    @GetMapping("/transfer")
    public String showTransferForm(Model model, Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        Account account = accountService.findAccountByCustomer(customer)
            .orElseThrow(() -> new RuntimeException("Account not found, cannot initiate transfer."));
        
        model.addAttribute("transactionRequest", new TransactionRequestDTO());
        model.addAttribute("currentBalance", account.getBalance());
        return "customer/transfer";
    }

    @PostMapping("/transfer")
    public String processTransfer(@ModelAttribute("transactionRequest") TransactionRequestDTO transactionRequest, Authentication authentication, RedirectAttributes redirectAttributes) {
        Customer customer = getAuthenticatedCustomer(authentication);
        Account fromAccount = accountService.findAccountByCustomer(customer)
            .orElseThrow(() -> new RuntimeException("Sender account could not be found."));

        try {
            transactionService.performPayment(
                fromAccount.getAccountNumber(),
                transactionRequest.getToAccountNumber(),
                transactionRequest.getAmount()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Transfer of ₹" + transactionRequest.getAmount() + " to account " + transactionRequest.getToAccountNumber() + " was successful!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transfer failed: " + e.getMessage());
        }
        return "redirect:/customer/dashboard";
    }

    @GetMapping("/transactions")
    public String showTransactions(Model model, Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        
        accountService.findAccountByCustomer(customer).ifPresent(account -> {
            // Use the corrected service method
            List<Transaction> transactions = transactionService.getTransactionHistory(account.getAccountNumber());
            model.addAttribute("transactions", transactions);
            model.addAttribute("accountNumber", account.getAccountNumber());
        });

        return "customer/transactions";
    }
}