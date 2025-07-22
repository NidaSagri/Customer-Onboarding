// In src/main/java/com/onboarding/repository/AccountRepository.java
package com.onboarding.repository;

import com.onboarding.model.Account;
import com.onboarding.model.Customer; // <-- Import Customer
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    
    // --- ADD THIS NEW METHOD ---
    Optional<Account> findByCustomer(Customer customer);
}

