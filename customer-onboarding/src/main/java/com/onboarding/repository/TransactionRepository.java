package com.onboarding.repository;

import com.onboarding.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions where the given account number is either the sender or the receiver,
     * ordered by the most recent transaction first.
     * @param accountNumber The account number to search for.
     * @return A list of transactions.
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccountNumber = :accountNumber OR t.toAccountNumber = :accountNumber ORDER BY t.timestamp DESC")
    List<Transaction> findTransactionsByAccountNumber(@Param("accountNumber") String accountNumber);

}