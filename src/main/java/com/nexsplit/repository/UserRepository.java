package com.nexsplit.repository;

import com.nexsplit.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User getUserByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByLastValidationCode(Integer lastValidationCode);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findActiveUserByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsActiveUserByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsActiveUserByUsername(@Param("username") String username);

    /**
     * Search users by email, username, first name, or last name.
     * Only returns active (non-deleted) users.
     * 
     * @param searchTerm The search term to match against
     * @param pageable   Pagination parameters
     * @return Page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "u.deletedAt IS NULL AND " +
            "u.status = 'ACTIVE' AND " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchActiveUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search users by email, username, first name, or last name.
     * Only returns active (non-deleted) users.
     * 
     * @param searchTerm The search term to match against
     * @return List of matching users (limited to 20 results)
     */
    @Query("SELECT u FROM User u WHERE " +
            "u.deletedAt IS NULL AND " +
            "u.status = 'ACTIVE' AND " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY u.firstName, u.lastName, u.username")
    List<User> searchActiveUsers(@Param("searchTerm") String searchTerm);

    /**
     * Search users by email only.
     * Only returns active (non-deleted) users.
     * 
     * @param email The email to search for
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
            "u.deletedAt IS NULL AND " +
            "u.status = 'ACTIVE' AND " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) " +
            "ORDER BY u.firstName, u.lastName, u.username")
    List<User> searchActiveUsersByEmail(@Param("email") String email);
}