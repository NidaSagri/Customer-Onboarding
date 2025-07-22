// src/main/java/com/onboarding/controller/TransactionController.java
package com.onboarding.controller;

import com.onboarding.dto.TransactionRequestDTO;
import com.onboarding.model.Transaction;
import com.onboarding.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/payment")
    public ResponseEntity<?> makePayment(@RequestBody TransactionRequestDTO request) {
        try {
            Transaction transaction = transactionService.performPayment(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount()
            );
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            // In a real app, handle specific exceptions
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}