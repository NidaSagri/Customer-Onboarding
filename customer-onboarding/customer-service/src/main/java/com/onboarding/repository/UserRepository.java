package com.onboarding.repository;

import com.onboarding.model.Customer;
import com.onboarding.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user directly by their unique username.
     * This is the primary method for authentication and validation.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their associated customer's email address.
     * This is an alternative login method. The explicit @Query is more reliable
     * than a long derived method name.
     */
    @Query("SELECT u FROM User u WHERE u.customer.email = :email")
    Optional<User> findByCustomerEmail(@Param("email") String email);

    /**
     * Finds a user by the associated Customer entity object.
     * This is used for the delete operation.
     */
    Optional<User> findByCustomer(Customer customer);

}