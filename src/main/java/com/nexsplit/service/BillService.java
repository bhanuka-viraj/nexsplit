package com.nexsplit.service;

import com.nexsplit.dto.bill.BillDto;
import com.nexsplit.dto.bill.BillSummaryDto;
import com.nexsplit.dto.bill.CreateBillRequest;
import com.nexsplit.dto.bill.UpdateBillRequest;
import com.nexsplit.dto.bill.BillParticipantDto;
import com.nexsplit.dto.bill.CreateBillParticipantRequest;
import com.nexsplit.dto.bill.UpdateBillParticipantRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for bill management operations.
 * 
 * This service provides comprehensive bill management functionality including
 * bill creation, recurring bill management, participant management, and
 * payment tracking using database views for optimal performance.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
public interface BillService {

    /**
     * Create a new bill.
     * 
     * @param request The bill creation request
     * @return Created bill DTO
     */
    BillDto createBill(CreateBillRequest request);

    /**
     * Get bill by ID.
     * 
     * @param billId The bill ID
     * @return Bill DTO
     */
    BillDto getBillById(String billId);

    /**
     * Update an existing bill.
     * 
     * @param billId  The bill ID
     * @param request The update request
     * @return Updated bill DTO
     */
    BillDto updateBill(String billId, UpdateBillRequest request);

    /**
     * Delete a bill (soft delete).
     * 
     * @param billId    The bill ID
     * @param deletedBy The user ID who deleted the bill
     */
    void deleteBill(String billId, String deletedBy);

    /**
     * Get all bills for a specific nex group.
     * 
     * @param nexId The nex ID
     * @return List of bill DTOs
     */
    List<BillDto> getBillsByNexId(String nexId);

    /**
     * Get all bills created by a specific user.
     * 
     * @param userId The user ID
     * @return List of bill DTOs
     */
    List<BillDto> getBillsByCreatedBy(String userId);

    /**
     * Get bills by status (paid/unpaid).
     * 
     * @param isPaid The payment status
     * @return List of bill DTOs
     */
    List<BillDto> getBillsByStatus(boolean isPaid);

    /**
     * Get bills by frequency.
     * 
     * @param frequency The bill frequency
     * @return List of bill DTOs
     */
    List<BillDto> getBillsByFrequency(String frequency);

    /**
     * Get recurring bills.
     * 
     * @return List of recurring bill DTOs
     */
    List<BillDto> getRecurringBills();

    /**
     * Get bills due within a date range.
     * 
     * @param startDate The start date
     * @param endDate   The end date
     * @return List of bill DTOs
     */
    List<BillDto> getBillsDueBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get overdue bills.
     * 
     * @return List of overdue bill DTOs
     */
    List<BillDto> getOverdueBills();

    /**
     * Mark a bill as paid.
     * 
     * @param billId The bill ID
     * @param paidAt The payment timestamp
     * @return Updated bill DTO
     */
    BillDto markBillAsPaid(String billId, LocalDateTime paidAt);

    /**
     * Mark a bill as unpaid.
     * 
     * @param billId The bill ID
     * @return Updated bill DTO
     */
    BillDto markBillAsUnpaid(String billId);

    /**
     * Process recurring bills.
     * This method should be called periodically to create new instances
     * of recurring bills based on their frequency.
     * 
     * @return Number of new bills created
     */
    int processRecurringBills();

    /**
     * Add a participant to a bill.
     * 
     * @param billId  The bill ID
     * @param request The participant creation request
     * @return Created participant DTO
     */
    BillParticipantDto addParticipant(String billId, CreateBillParticipantRequest request);

    /**
     * Update a bill participant.
     * 
     * @param billId  The bill ID
     * @param userId  The user ID
     * @param request The update request
     * @return Updated participant DTO
     */
    BillParticipantDto updateParticipant(String billId, String userId, UpdateBillParticipantRequest request);

    /**
     * Remove a participant from a bill.
     * 
     * @param billId    The bill ID
     * @param userId    The user ID
     * @param deletedBy The user ID who removed the participant
     */
    void removeParticipant(String billId, String userId, String deletedBy);

    /**
     * Get all participants for a bill.
     * 
     * @param billId The bill ID
     * @return List of participant DTOs
     */
    List<BillParticipantDto> getBillParticipants(String billId);

    /**
     * Get bills where a user is a participant.
     * 
     * @param userId The user ID
     * @return List of bill DTOs
     */
    List<BillDto> getBillsByParticipant(String userId);

    /**
     * Mark a participant as paid.
     * 
     * @param billId The bill ID
     * @param userId The user ID
     * @param paidAt The payment timestamp
     * @return Updated participant DTO
     */
    BillParticipantDto markParticipantAsPaid(String billId, String userId, LocalDateTime paidAt);

    /**
     * Mark a participant as unpaid.
     * 
     * @param billId The bill ID
     * @param userId The user ID
     * @return Updated participant DTO
     */
    BillParticipantDto markParticipantAsUnpaid(String billId, String userId);

    /**
     * Get bill summary for a nex group.
     * 
     * @param nexId The nex ID
     * @return List of bill summary DTOs
     */
    List<BillSummaryDto> getBillSummaryByNexId(String nexId);

    /**
     * Get bill summary for a user.
     * 
     * @param userId The user ID
     * @return List of bill summary DTOs
     */
    List<BillSummaryDto> getBillSummaryByUserId(String userId);

    /**
     * Calculate total amount owed by a user for a bill.
     * 
     * @param billId The bill ID
     * @param userId The user ID
     * @return Total amount owed
     */
    BigDecimal calculateUserOwedAmount(String billId, String userId);

    /**
     * Calculate total amount paid by a user for a bill.
     * 
     * @param billId The bill ID
     * @param userId The user ID
     * @return Total amount paid
     */
    BigDecimal calculateUserPaidAmount(String billId, String userId);

    /**
     * Get bill statistics for a nex group.
     * 
     * @param nexId The nex ID
     * @return Bill statistics
     */
    BillStatistics getBillStatisticsByNexId(String nexId);

    /**
     * Get bill statistics for a user.
     * 
     * @param userId The user ID
     * @return Bill statistics
     */
    BillStatistics getBillStatisticsByUserId(String userId);

    /**
     * Get all bills.
     * 
     * @return List of bill DTOs
     */
    List<BillDto> getAllBills();

    /**
     * Get all bills with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of bill DTOs
     */
    org.springframework.data.domain.Page<BillDto> getAllBills(org.springframework.data.domain.Pageable pageable);

    /**
     * Get bills by user ID with pagination.
     * 
     * @param userId   The user ID
     * @param pageable Pagination parameters
     * @return Page of bill DTOs
     */
    org.springframework.data.domain.Page<BillDto> getBillsByUserId(String userId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Get bills by nex ID with pagination.
     * 
     * @param nexId    The nex ID
     * @param pageable Pagination parameters
     * @return Page of bill DTOs
     */
    org.springframework.data.domain.Page<BillDto> getBillsByNexId(String nexId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Add a participant to a bill with simplified parameters.
     * 
     * @param billId      The bill ID
     * @param userId      The user ID
     * @param shareAmount The share amount
     * @return Created participant DTO
     */
    BillParticipantDto addParticipant(String billId, String userId, Double shareAmount);

    /**
     * Remove a participant from a bill.
     * 
     * @param billId The bill ID
     * @param userId The user ID
     */
    void removeParticipant(String billId, String userId);

    /**
     * Mark a participant as paid.
     * 
     * @param billId The bill ID
     * @param userId The user ID
     * @return Updated participant DTO
     */
    BillParticipantDto markParticipantAsPaid(String billId, String userId);

    /**
     * Get bill analytics.
     * 
     * @param nexId The nex ID
     * @return Bill analytics
     */
    Object getBillAnalytics(String nexId);

    /**
     * Delete a bill.
     * 
     * @param billId The bill ID
     */
    void deleteBill(String billId);

    /**
     * Bill statistics data class.
     */
    class BillStatistics {
        private int totalBills;
        private int paidBills;
        private int unpaidBills;
        private int overdueBills;
        private BigDecimal totalBillAmount;
        private BigDecimal totalPaidAmount;
        private BigDecimal totalUnpaidAmount;
        private BigDecimal totalOverdueAmount;
        private int totalParticipants;
        private int paidParticipants;
        private int unpaidParticipants;

        public BillStatistics(int totalBills, int paidBills, int unpaidBills, int overdueBills,
                BigDecimal totalBillAmount, BigDecimal totalPaidAmount,
                BigDecimal totalUnpaidAmount, BigDecimal totalOverdueAmount,
                int totalParticipants, int paidParticipants, int unpaidParticipants) {
            this.totalBills = totalBills;
            this.paidBills = paidBills;
            this.unpaidBills = unpaidBills;
            this.overdueBills = overdueBills;
            this.totalBillAmount = totalBillAmount;
            this.totalPaidAmount = totalPaidAmount;
            this.totalUnpaidAmount = totalUnpaidAmount;
            this.totalOverdueAmount = totalOverdueAmount;
            this.totalParticipants = totalParticipants;
            this.paidParticipants = paidParticipants;
            this.unpaidParticipants = unpaidParticipants;
        }

        // Getters and setters
        public int getTotalBills() {
            return totalBills;
        }

        public void setTotalBills(int totalBills) {
            this.totalBills = totalBills;
        }

        public int getPaidBills() {
            return paidBills;
        }

        public void setPaidBills(int paidBills) {
            this.paidBills = paidBills;
        }

        public int getUnpaidBills() {
            return unpaidBills;
        }

        public void setUnpaidBills(int unpaidBills) {
            this.unpaidBills = unpaidBills;
        }

        public int getOverdueBills() {
            return overdueBills;
        }

        public void setOverdueBills(int overdueBills) {
            this.overdueBills = overdueBills;
        }

        public BigDecimal getTotalBillAmount() {
            return totalBillAmount;
        }

        public void setTotalBillAmount(BigDecimal totalBillAmount) {
            this.totalBillAmount = totalBillAmount;
        }

        public BigDecimal getTotalPaidAmount() {
            return totalPaidAmount;
        }

        public void setTotalPaidAmount(BigDecimal totalPaidAmount) {
            this.totalPaidAmount = totalPaidAmount;
        }

        public BigDecimal getTotalUnpaidAmount() {
            return totalUnpaidAmount;
        }

        public void setTotalUnpaidAmount(BigDecimal totalUnpaidAmount) {
            this.totalUnpaidAmount = totalUnpaidAmount;
        }

        public BigDecimal getTotalOverdueAmount() {
            return totalOverdueAmount;
        }

        public void setTotalOverdueAmount(BigDecimal totalOverdueAmount) {
            this.totalOverdueAmount = totalOverdueAmount;
        }

        public int getTotalParticipants() {
            return totalParticipants;
        }

        public void setTotalParticipants(int totalParticipants) {
            this.totalParticipants = totalParticipants;
        }

        public int getPaidParticipants() {
            return paidParticipants;
        }

        public void setPaidParticipants(int paidParticipants) {
            this.paidParticipants = paidParticipants;
        }

        public int getUnpaidParticipants() {
            return unpaidParticipants;
        }

        public void setUnpaidParticipants(int unpaidParticipants) {
            this.unpaidParticipants = unpaidParticipants;
        }
    }
}
