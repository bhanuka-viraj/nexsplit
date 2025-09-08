package com.nexsplit.repository;

import com.nexsplit.model.Split;
import com.nexsplit.model.SplitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Split entity.
 * Provides database operations for split management.
 */
@Repository
public interface SplitRepository extends JpaRepository<Split, SplitId> {

    /**
     * Find splits by expense ID.
     * 
     * @param expenseId The expense ID
     * @return List of splits
     */
    List<Split> findByIdExpenseIdOrderByIdUserId(String expenseId);

    /**
     * Find splits by user ID.
     * 
     * @param userId The user ID
     * @return List of splits
     */
    List<Split> findByIdUserIdOrderByIdExpenseId(String userId);

    /**
     * Find split by expense ID and user ID.
     * 
     * @param expenseId The expense ID
     * @param userId    The user ID
     * @return Optional split
     */
    Optional<Split> findByIdExpenseIdAndIdUserId(String expenseId, String userId);

    /**
     * Find splits by expense ID and user ID list.
     * 
     * @param expenseId The expense ID
     * @param userIds   List of user IDs
     * @return List of splits
     */
    @Query("SELECT s FROM Split s WHERE s.id.expenseId = :expenseId AND s.id.userId IN :userIds")
    List<Split> findByIdExpenseIdAndIdUserIdIn(@Param("expenseId") String expenseId,
            @Param("userIds") List<String> userIds);

    /**
     * Delete splits by expense ID.
     * 
     * @param expenseId The expense ID
     */
    void deleteByIdExpenseId(String expenseId);

    /**
     * Count splits by expense ID.
     * 
     * @param expenseId The expense ID
     * @return Count of splits
     */
    long countByIdExpenseId(String expenseId);

    /**
     * Count splits by user ID.
     * 
     * @param userId The user ID
     * @return Count of splits
     */
    long countByIdUserId(String userId);

    /**
     * Check if split exists for expense and user.
     * 
     * @param expenseId The expense ID
     * @param userId    The user ID
     * @return True if split exists
     */
    boolean existsByIdExpenseIdAndIdUserId(String expenseId, String userId);
}
