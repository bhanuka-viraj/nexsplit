package com.nexsplit.repository;

import com.nexsplit.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Bill entity operations.
 *
 * This repository provides data access methods for bill management including
 * CRUD operations, filtering by various criteria, and pagination support.
 *
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, String> {

    /**
     * Find all bills that are not deleted.
     *
     * @return List of non-deleted bills
     */
    List<Bill> findByIsDeletedFalse();

    /**
     * Find all bills that are not deleted with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of non-deleted bills
     */
    Page<Bill> findByIsDeletedFalse(Pageable pageable);

    /**
     * Find bills by nex ID that are not deleted.
     *
     * @param nexId The nex ID
     * @return List of bills for the specified nex
     */
    List<Bill> findByNexIdAndIsDeletedFalse(String nexId);

    /**
     * Find bills by nex ID that are not deleted with pagination.
     *
     * @param nexId    The nex ID
     * @param pageable Pagination parameters
     * @return Page of bills for the specified nex
     */
    Page<Bill> findByNexIdAndIsDeletedFalse(String nexId, Pageable pageable);

    /**
     * Find bills by creator that are not deleted.
     *
     * @param createdBy The user ID who created the bills
     * @return List of bills created by the specified user
     */
    List<Bill> findByCreatedByAndIsDeletedFalse(String createdBy);

    /**
     * Find bills by creator that are not deleted with pagination.
     *
     * @param createdBy The user ID who created the bills
     * @param pageable  Pagination parameters
     * @return Page of bills created by the specified user
     */
    Page<Bill> findByCreatedByAndIsDeletedFalse(String createdBy, Pageable pageable);

    /**
     * Find bills by payment status that are not deleted.
     *
     * @param isPaid The payment status
     * @return List of bills with the specified payment status
     */
    List<Bill> findByIsPaidAndIsDeletedFalse(boolean isPaid);

    /**
     * Find bills by frequency that are not deleted.
     *
     * @param frequency The bill frequency
     * @return List of bills with the specified frequency
     */
    List<Bill> findByFrequencyAndIsDeletedFalse(String frequency);

    /**
     * Find recurring bills that are not deleted.
     *
     * @return List of recurring bills
     */
    List<Bill> findByIsRecurringTrueAndIsDeletedFalse();

    /**
     * Find bills due between specified dates that are not deleted.
     *
     * @param startDate The start date
     * @param endDate   The end date
     * @return List of bills due between the specified dates
     */
    List<Bill> findByDueDateBetweenAndIsDeletedFalse(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find overdue bills that are not deleted.
     *
     * @return List of overdue bills
     */
    @Query("SELECT b FROM Bill b WHERE b.isDeleted = false AND b.isPaid = false AND b.dueDate < :currentDate")
    List<Bill> findOverdueBills(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find overdue bills that are not deleted (using current time).
     *
     * @return List of overdue bills
     */
    @Query("SELECT b FROM Bill b WHERE b.isDeleted = false AND b.isPaid = false AND b.dueDate < CURRENT_TIMESTAMP")
    List<Bill> findOverdueBills();

    /**
     * Find bills by amount range that are not deleted.
     *
     * @param minAmount The minimum amount
     * @param maxAmount The maximum amount
     * @return List of bills within the specified amount range
     */
    @Query("SELECT b FROM Bill b WHERE b.isDeleted = false AND b.amount BETWEEN :minAmount AND :maxAmount")
    List<Bill> findByAmountBetweenAndIsDeletedFalse(@Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount);

    /**
     * Count bills by nex ID that are not deleted.
     *
     * @param nexId The nex ID
     * @return Count of bills for the specified nex
     */
    long countByNexIdAndIsDeletedFalse(String nexId);

    /**
     * Count bills by creator that are not deleted.
     *
     * @param createdBy The user ID who created the bills
     * @return Count of bills created by the specified user
     */
    long countByCreatedByAndIsDeletedFalse(String createdBy);

    /**
     * Count bills by payment status that are not deleted.
     *
     * @param isPaid The payment status
     * @return Count of bills with the specified payment status
     */
    long countByIsPaidAndIsDeletedFalse(boolean isPaid);

    /**
     * Count recurring bills that are not deleted.
     *
     * @return Count of recurring bills
     */
    long countByIsRecurringTrueAndIsDeletedFalse();

    /**
     * Count overdue bills that are not deleted.
     *
     * @return Count of overdue bills
     */
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.isDeleted = false AND b.isPaid = false AND b.dueDate < CURRENT_TIMESTAMP")
    long countOverdueBills();

    /**
     * Find bills created between specified dates that are not deleted.
     *
     * @param startDate The start date
     * @param endDate   The end date
     * @return List of bills created between the specified dates
     */
    List<Bill> findByCreatedAtBetweenAndIsDeletedFalse(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find bills by title containing the specified text that are not deleted.
     *
     * @param title The title text to search for
     * @return List of bills with titles containing the specified text
     */
    List<Bill> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);

    /**
     * Find bills by notes containing the specified text that are not deleted.
     *
     * @param notes The notes text to search for
     * @return List of bills with notes containing the specified text
     */
    List<Bill> findByNotesContainingIgnoreCaseAndIsDeletedFalse(String notes);

}
