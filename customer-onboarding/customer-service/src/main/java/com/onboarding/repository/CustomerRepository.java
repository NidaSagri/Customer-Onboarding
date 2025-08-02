package com.onboarding.repository;

import com.onboarding.model.Customer;
import com.onboarding.model.KycStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
	Optional<Customer> findByEmail(String email);

    /**
     * Finds a customer by their unique PAN number.
     */
    Optional<Customer> findByPan(String pan);

    /**
     * Finds a customer by their unique Aadhaar number.
     */
    Optional<Customer> findByAadhaar(String aadhaar);
    
    /**
     * Finds all customers that have a specific KYC status.
     * This is used by the admin dashboard to show pending requests.
     *
     * @param status The KycStatus to search for (e.g., PENDING).
     * @return A list of matching customers.
     */
    List<Customer> findCustomersByKycStatus(KycStatus status);

    /**
     * A flexible search method for the admin panel. It searches across multiple fields.
     * Note: For Oracle, CAST(c.id AS string) should be TO_CHAR(c.id).
     * This query uses standard SQL functions that are generally compatible.
     *
     * @param keyword The search term entered by the admin.
     * @return A list of customers matching the keyword.
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.pan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(c.id AS string) LIKE CONCAT('%', :keyword, '%')")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);
    
    // --- NEW METHODS ---

    /**
     * A powerful search query that looks for a keyword in multiple fields.
     * It supports pagination via the Pageable object.
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.pan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(c.id AS string) LIKE CONCAT('%', :keyword, '%')")
    Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Counts customers by their KYC status. Used for the dashboard stat cards.
     */
    long countByKycStatus(KycStatus status);
    
    Page<Customer> findByKycStatus(KycStatus status, Pageable pageable);
    
    List<Customer> findTop5ByKycStatusOrderByIdDesc(KycStatus status);

}