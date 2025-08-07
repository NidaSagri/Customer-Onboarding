package com.onboarding.controller;

import com.onboarding.dto.AccountDTO;
import com.onboarding.feign.AccountClient;
import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;
import com.onboarding.service.CustomerService;
import java.util.HashMap; // <-- IMPORT HASHMAP
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CustomerService customerService;
    private final AccountClient accountClient;

    public AdminController(CustomerService customerService, AccountClient accountClient) {
        this.customerService = customerService;
        this.accountClient = accountClient;
    }
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = StringUtils.hasText(keyword)
            ? customerService.searchCustomers(keyword, pageable)
            : customerService.findAllCustomers(pageable);

        // --- NEW LOGIC TO FETCH ACCOUNT DETAILS ---
        if (!customerPage.isEmpty()) {
            List<Long> customerIds = customerPage.getContent().stream()
                    .map(Customer::getId)
                    .collect(Collectors.toList());

            List<AccountDTO> accounts = accountClient.getAccountsByCustomerIds(customerIds);

            // --- THE NEW APPROACH: Use a simple for-loop to build the map ---
            // This is more verbose but avoids the specific IDE compiler issue.
            Map<Long, AccountDTO> accountMap = new HashMap<>();
            for (AccountDTO account : accounts) {
                // The putIfAbsent method ensures that if we get duplicate customer IDs,
                // we only keep the first account we find, safely handling duplicates.
                accountMap.putIfAbsent(account.getCustomerId(), account);
            }
            
            model.addAttribute("accountMap", accountMap);
        }
        // --- END OF NEW LOGIC ---

        model.addAttribute("customers", customerPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCustomers", customerService.countTotalCustomers());
        model.addAttribute("pendingKyc", customerService.countCustomersByKycStatus(KycStatus.PENDING));
        model.addAttribute("verifiedAccounts", customerService.countCustomersByKycStatus(KycStatus.VERIFIED));
        return "admin/dashboard";
    }

    // --- The rest of the controller methods are correct and do not need changes ---

    @GetMapping("/customer/{id}")
    public String customerDetails(@PathVariable Long id, Model model) {
        Customer customer = customerService.findCustomerById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
        model.addAttribute("customer", customer);
        try {
            AccountDTO account = accountClient.getAccountByCustomerId(id);
            model.addAttribute("account", account);
        } catch (Exception e) {
            // No account found, which is a valid state.
        }
        return "admin/customer-details";
    }
    

    @PostMapping("/customer/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("message", "Customer " + id + " has been deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting customer: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/customer/{id}/edit")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Customer customer = customerService.findCustomerById(id)
          .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + id));
        model.addAttribute("customer", customer);
        return "admin/edit-customer";
    }

    @PostMapping("/customer/{id}/edit")
    public String updateCustomer(@PathVariable("id") long id, @ModelAttribute("customer") Customer customer, RedirectAttributes redirectAttributes) {
        try {
            customerService.updateCustomer(id, customer);
            redirectAttributes.addFlashAttribute("message", "Customer " + id + " has been updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating customer: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}