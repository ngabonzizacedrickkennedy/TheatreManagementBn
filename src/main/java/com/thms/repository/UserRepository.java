package com.thms.repository;

import com.thms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    // Add these methods to your existing UserRepository interface

    /**
     * Search users by multiple fields
     */
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String username, String email, String firstName, String lastName);

    /**
     * Find users by role
     */
    List<User> findByRole(User.Role role);

    /**
     * Check if username exists excluding a specific user ID
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

    /**
     * Check if email exists excluding a specific user ID
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Find all users with pagination and sorting
     */
    Page<User> findAll(Pageable pageable);
}