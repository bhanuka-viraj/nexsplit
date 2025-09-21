package com.nexsplit.repository;

import com.nexsplit.model.view.UserBalanceView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for user balance operations using database views.
 * 
 * This repository provides optimized queries for user balance calculations
 * using the user_balance_view as the primary data access method.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalanceView, String> {

    /**
     * Find balance information for a specific user.
     * 
     * @param userId The user ID
     * @return User balance information
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE user_id = :userId
            """, nativeQuery = true)
    Optional<UserBalanceView> findByUserId(@Param("userId") String userId);

    /**
     * Find all users with positive net balance (users who are owed money).
     * 
     * @return List of users with positive net balance
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE net_balance > 0
            ORDER BY net_balance DESC
            """, nativeQuery = true)
    List<UserBalanceView> findUsersWithPositiveBalance();

    /**
     * Find all users with negative net balance (users who owe money).
     * 
     * @return List of users with negative net balance
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE net_balance < 0
            ORDER BY net_balance ASC
            """, nativeQuery = true)
    List<UserBalanceView> findUsersWithNegativeBalance();

    /**
     * Find all users with zero net balance (users who are balanced).
     * 
     * @return List of users with zero net balance
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE net_balance = 0
            ORDER BY user_name ASC
            """, nativeQuery = true)
    List<UserBalanceView> findUsersWithZeroBalance();

    /**
     * Find users with active debts.
     * 
     * @return List of users with active debts
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE active_debt_count > 0
            ORDER BY active_debt_count DESC
            """, nativeQuery = true)
    List<UserBalanceView> findUsersWithActiveDebts();

    /**
     * Find users with active credits.
     * 
     * @return List of users with active credits
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE active_credit_count > 0
            ORDER BY active_credit_count DESC
            """, nativeQuery = true)
    List<UserBalanceView> findUsersWithActiveCredits();

    /**
     * Find users with net balance within a range.
     * 
     * @param minBalance The minimum balance
     * @param maxBalance The maximum balance
     * @return List of users with balance within the range
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE net_balance BETWEEN :minBalance AND :maxBalance
            ORDER BY net_balance DESC
            """, nativeQuery = true)
    List<UserBalanceView> findByBalanceRange(@Param("minBalance") BigDecimal minBalance,
            @Param("maxBalance") BigDecimal maxBalance);

    /**
     * Find users with total debt above a threshold.
     * 
     * @param threshold The debt threshold
     * @return List of users with debt above threshold
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE total_debt > :threshold
            ORDER BY total_debt DESC
            """, nativeQuery = true)
    List<UserBalanceView> findUsersWithDebtAbove(@Param("threshold") BigDecimal threshold);

    /**
     * Find users with total credit above a threshold.
     * 
     * @param threshold The credit threshold
     * @return List of users with credit above threshold
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE total_credit > :threshold
            ORDER BY total_credit DESC
            """, nativeQuery = true)
    List<UserBalanceView> findUsersWithCreditAbove(@Param("threshold") BigDecimal threshold);

    /**
     * Get overall balance statistics.
     * 
     * @return Overall balance statistics
     */
    @Query(value = """
            SELECT
                COUNT(*) as total_users,
                SUM(total_debt) as total_debt_amount,
                SUM(total_credit) as total_credit_amount,
                SUM(net_balance) as total_net_balance,
                AVG(net_balance) as average_net_balance,
                MAX(net_balance) as max_net_balance,
                MIN(net_balance) as min_net_balance,
                COUNT(CASE WHEN net_balance > 0 THEN 1 END) as users_with_positive_balance,
                COUNT(CASE WHEN net_balance < 0 THEN 1 END) as users_with_negative_balance,
                COUNT(CASE WHEN net_balance = 0 THEN 1 END) as users_with_zero_balance
            FROM user_balance_view
            """, nativeQuery = true)
    Optional<Object[]> getOverallBalanceStatistics();

    /**
     * Find all user balances for a specific nex group.
     * 
     * @param nexId The nex ID
     * @return List of user balance records
     */
    @Query(value = """
            SELECT * FROM user_balance_view
            WHERE nex_id = :nexId
            ORDER BY net_balance DESC
            """, nativeQuery = true)
    List<UserBalanceView> findByNexId(@Param("nexId") String nexId);
}
