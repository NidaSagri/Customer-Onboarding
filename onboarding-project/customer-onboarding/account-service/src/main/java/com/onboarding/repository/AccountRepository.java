package com.onboarding.repository;

import com.onboarding.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // <-- IMPORT
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByCustomerId(Long customerId);
    List<Account> findByCustomerIdIn(List<Long> customerIds);

    // --- NEW METHOD ---
    List<Account> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}