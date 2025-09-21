package com.nexsplit.repository;

import com.nexsplit.model.view.NexAnalyticsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for nex analytics operations using database views.
 * 
 * This repository provides optimized queries for nex analytics and insights
 * using the nex_analytics_view as the primary data access method.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface NexAnalyticsRepository extends JpaRepository<NexAnalyticsView, String> {

        /**
         * Find analytics for a specific nex group.
         * 
         * @param nexId The nex ID
         * @return Nex analytics information
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE nex_id = :nexId
                        """, nativeQuery = true)
        Optional<NexAnalyticsView> findByNexId(@Param("nexId") String nexId);

        /**
         * Find analytics for nex groups created by a specific user.
         * 
         * @param userId The user ID
         * @return List of nex analytics records
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE created_by = :userId
                        ORDER BY nex_created_at DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findByCreatedBy(@Param("userId") String userId);

        /**
         * Find active nex groups (not archived).
         * 
         * @return List of active nex analytics records
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE is_archived = false
                        ORDER BY nex_created_at DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findActiveNexGroups();

        /**
         * Find archived nex groups.
         * 
         * @return List of archived nex analytics records
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE is_archived = true
                        ORDER BY nex_created_at DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findArchivedNexGroups();

        /**
         * Find nex groups by type.
         * 
         * @param nexType The nex type
         * @return List of nex analytics records
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE nex_type = :nexType
                        ORDER BY nex_created_at DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findByNexType(@Param("nexType") String nexType);

        /**
         * Find nex groups by settlement type.
         * 
         * @param settlementType The settlement type
         * @return List of nex analytics records
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE settlement_type = :settlementType
                        ORDER BY nex_created_at DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findBySettlementType(@Param("settlementType") String settlementType);

        /**
         * Find nex groups with total expense amount above threshold.
         * 
         * @param threshold The amount threshold
         * @return List of nex analytics records
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE total_expense_amount > :threshold
                        ORDER BY total_expense_amount DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findByTotalExpenseAmountAbove(@Param("threshold") BigDecimal threshold);

        /**
         * Find nex groups with member count above threshold.
         * 
         * @param threshold The member count threshold
         * @return List of nex analytics records
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE total_members > :threshold
                        ORDER BY total_members DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findByMemberCountAbove(@Param("threshold") Integer threshold);

        /**
         * Find nex groups with unsettled debts.
         * 
         * @return List of nex analytics records with unsettled debts
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE unsettled_debts > 0
                        ORDER BY unsettled_debt_amount DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findWithUnsettledDebts();

        /**
         * Find nex groups with recent activity.
         * 
         * @param days The number of days to look back
         * @return List of nex analytics records with recent activity
         */
        @Query(value = """
                        SELECT * FROM nex_analytics_view
                        WHERE last_expense_date > :cutoffDate
                        OR last_debt_date > :cutoffDate
                        OR last_bill_date > :cutoffDate
                        ORDER BY GREATEST(last_expense_date, last_debt_date, last_bill_date) DESC
                        """, nativeQuery = true)
        List<NexAnalyticsView> findWithRecentActivity(@Param("cutoffDate") LocalDateTime cutoffDate);

        /**
         * Get overall nex analytics statistics.
         * 
         * @return Overall nex analytics statistics
         */
        @Query(value = """
                        SELECT
                            COUNT(*) as total_nex_groups,
                            COUNT(CASE WHEN is_archived = false THEN 1 END) as active_nex_groups,
                            COUNT(CASE WHEN is_archived = true THEN 1 END) as archived_nex_groups,
                            COUNT(CASE WHEN nex_type = 'PERSONAL' THEN 1 END) as personal_nex_groups,
                            COUNT(CASE WHEN nex_type = 'GROUP' THEN 1 END) as group_nex_groups,
                            COUNT(CASE WHEN settlement_type = 'DETAILED' THEN 1 END) as detailed_settlement_groups,
                            COUNT(CASE WHEN settlement_type = 'SIMPLIFIED' THEN 1 END) as simplified_settlement_groups,
                            SUM(total_members) as total_members_across_all_groups,
                            SUM(total_expenses) as total_expenses_across_all_groups,
                            SUM(total_expense_amount) as total_expense_amount_across_all_groups,
                            AVG(total_expense_amount) as average_expense_amount_per_group,
                            SUM(unsettled_debts) as total_unsettled_debts,
                            SUM(unsettled_debt_amount) as total_unsettled_debt_amount
                        FROM nex_analytics_view
                        """, nativeQuery = true)
        Optional<Object[]> getOverallNexAnalyticsStatistics();
}
