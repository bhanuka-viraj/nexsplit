package com.nexsplit.service.impl;

import com.nexsplit.dto.bill.BillDto;
import com.nexsplit.dto.bill.BillSummaryDto;
import com.nexsplit.dto.bill.CreateBillRequest;
import com.nexsplit.dto.bill.UpdateBillRequest;
import com.nexsplit.dto.bill.BillParticipantDto;
import com.nexsplit.dto.bill.CreateBillParticipantRequest;
import com.nexsplit.dto.bill.UpdateBillParticipantRequest;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.mapper.bill.BillMapStruct;
import com.nexsplit.mapper.bill.BillParticipantMapStruct;
import com.nexsplit.model.Bill;
import com.nexsplit.model.BillParticipant;
import com.nexsplit.model.BillParticipantId;
import com.nexsplit.repository.BillRepository;
import com.nexsplit.repository.BillParticipantRepository;
import com.nexsplit.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for bill management operations.
 *
 * This service provides comprehensive bill management functionality including
 * bill creation, recurring bill management, participant management, and
 * payment tracking using database views for optimal performance.
 *
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final BillParticipantRepository billParticipantRepository;
    private final BillMapStruct billMapStruct;
    private final BillParticipantMapStruct billParticipantMapStruct;

    @Override
    public BillDto createBill(CreateBillRequest request) {
        log.info("Creating new bill: {}", request);

        Bill bill = billMapStruct.toEntity(request);
        bill = billRepository.save(bill);

        log.info("Bill created successfully with ID: {}", bill.getId());
        return billMapStruct.toDto(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillDto getBillById(String billId) {
        log.debug("Getting bill by ID: {}", billId);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> EntityNotFoundException.billNotFound(billId));

        return billMapStruct.toDto(bill);
    }

    @Override
    public BillDto updateBill(String billId, UpdateBillRequest request) {
        log.info("Updating bill: {} with request: {}", billId, request);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> EntityNotFoundException.billNotFound(billId));

        billMapStruct.updateEntityFromRequest(request, bill);
        bill = billRepository.save(bill);

        log.info("Bill updated successfully: {}", billId);
        return billMapStruct.toDto(bill);
    }

    @Override
    public void deleteBill(String billId, String deletedBy) {
        log.info("Deleting bill: {} by user: {}", billId, deletedBy);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> EntityNotFoundException.billNotFound(billId));

        bill.setIsDeleted(true);
        bill.setDeletedAt(LocalDateTime.now());
        bill.setDeletedBy(deletedBy);
        billRepository.save(bill);

        log.info("Bill deleted successfully: {}", billId);
    }

    @Override
    public void deleteBill(String billId) {
        log.info("Deleting bill: {}", billId);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> EntityNotFoundException.billNotFound(billId));

        bill.setIsDeleted(true);
        bill.setDeletedAt(LocalDateTime.now());
        billRepository.save(bill);

        log.info("Bill deleted successfully: {}", billId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getBillsByNexId(String nexId) {
        log.debug("Getting bills by nex ID: {}", nexId);

        List<Bill> bills = billRepository.findByNexIdAndIsDeletedFalse(nexId);
        return bills.stream()
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getBillsByCreatedBy(String userId) {
        log.debug("Getting bills by created by: {}", userId);

        List<Bill> bills = billRepository.findByCreatedByAndIsDeletedFalse(userId);
        return bills.stream()
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getBillsByStatus(boolean isPaid) {
        log.debug("Getting bills by status: {}", isPaid);

        List<Bill> bills = billRepository.findByIsPaidAndIsDeletedFalse(isPaid);
        return bills.stream()
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getBillsByFrequency(String frequency) {
        log.debug("Getting bills by frequency: {}", frequency);

        List<Bill> bills = billRepository.findByFrequencyAndIsDeletedFalse(frequency);
        return bills.stream()
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getRecurringBills() {
        log.debug("Getting recurring bills");

        List<Bill> bills = billRepository.findByIsRecurringTrueAndIsDeletedFalse();
        return bills.stream()
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getBillsDueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting bills due between: {} and {}", startDate, endDate);

        List<Bill> bills = billRepository.findByDueDateBetweenAndIsDeletedFalse(startDate, endDate);
        return bills.stream()
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getOverdueBills() {
        log.debug("Getting overdue bills");

        List<Bill> bills = billRepository.findOverdueBills();
        return bills.stream()
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BillDto markBillAsPaid(String billId, LocalDateTime paidAt) {
        log.info("Marking bill as paid: {} at {}", billId, paidAt);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> EntityNotFoundException.billNotFound(billId));

        bill.setIsPaid(true);
        // Note: Bill entity doesn't have paidAt field, only BillParticipant does
        bill = billRepository.save(bill);

        log.info("Bill marked as paid successfully: {}", billId);
        return billMapStruct.toDto(bill);
    }

    @Override
    public BillDto markBillAsUnpaid(String billId) {
        log.info("Marking bill as unpaid: {}", billId);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> EntityNotFoundException.billNotFound(billId));

        bill.setIsPaid(false);
        // Note: Bill entity doesn't have paidAt field, only BillParticipant does
        bill = billRepository.save(bill);

        log.info("Bill marked as unpaid successfully: {}", billId);
        return billMapStruct.toDto(bill);
    }

    @Override
    public int processRecurringBills() {
        log.info("Processing recurring bills");

        List<Bill> recurringBills = billRepository.findByIsRecurringTrueAndIsDeletedFalse();
        int processedCount = 0;

        for (Bill bill : recurringBills) {
            if (shouldCreateNewBill(bill)) {
                createNewRecurringBill(bill);
                processedCount++;
            }
        }

        log.info("Processed {} recurring bills", processedCount);
        return processedCount;
    }

    @Override
    public BillParticipantDto addParticipant(String billId, CreateBillParticipantRequest request) {
        log.info("Adding participant to bill: {} with request: {}", billId, request);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> EntityNotFoundException.billNotFound(billId));

        BillParticipant participant = billParticipantMapStruct.toEntity(request);
        participant.setBill(bill);
        participant = billParticipantRepository.save(participant);

        log.info("Participant added successfully to bill: {}", billId);
        return billParticipantMapStruct.toDto(participant);
    }

    @Override
    public BillParticipantDto addParticipant(String billId, String userId, Double shareAmount) {
        log.info("Adding participant to bill: {} user: {} amount: {}", billId, userId, shareAmount);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> EntityNotFoundException.billNotFound(billId));

        BillParticipant participant = new BillParticipant();
        participant.setBill(bill);
        // Set the composite ID
        BillParticipantId id = new BillParticipantId();
        id.setBillId(billId);
        id.setUserId(userId);
        participant.setId(id);
        participant.setShareAmount(BigDecimal.valueOf(shareAmount));
        participant.setPaid(false);
        participant = billParticipantRepository.save(participant);

        log.info("Participant added successfully to bill: {}", billId);
        return billParticipantMapStruct.toDto(participant);
    }

    @Override
    public BillParticipantDto updateParticipant(String billId, String userId, UpdateBillParticipantRequest request) {
        log.info("Updating participant in bill: {} user: {} with request: {}", billId, userId, request);

        BillParticipant participant = billParticipantRepository.findByBillIdAndUserId(billId, userId)
                .orElseThrow(() -> EntityNotFoundException.billParticipantNotFound(billId, userId));

        billParticipantMapStruct.updateEntityFromRequest(request, participant);
        participant = billParticipantRepository.save(participant);

        log.info("Participant updated successfully in bill: {}", billId);
        return billParticipantMapStruct.toDto(participant);
    }

    @Override
    public void removeParticipant(String billId, String userId, String deletedBy) {
        log.info("Removing participant from bill: {} user: {} by: {}", billId, userId, deletedBy);

        BillParticipant participant = billParticipantRepository.findByBillIdAndUserId(billId, userId)
                .orElseThrow(() -> EntityNotFoundException.billParticipantNotFound(billId, userId));

        participant.setIsDeleted(true);
        participant.setDeletedAt(LocalDateTime.now());
        participant.setDeletedBy(deletedBy);
        billParticipantRepository.save(participant);

        log.info("Participant removed successfully from bill: {}", billId);
    }

    @Override
    public void removeParticipant(String billId, String userId) {
        log.info("Removing participant from bill: {} user: {}", billId, userId);

        BillParticipant participant = billParticipantRepository.findByBillIdAndUserId(billId, userId)
                .orElseThrow(() -> EntityNotFoundException.billParticipantNotFound(billId, userId));

        participant.setIsDeleted(true);
        participant.setDeletedAt(LocalDateTime.now());
        billParticipantRepository.save(participant);

        log.info("Participant removed successfully from bill: {}", billId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillParticipantDto> getBillParticipants(String billId) {
        log.debug("Getting participants for bill: {}", billId);

        List<BillParticipant> participants = billParticipantRepository.findByBillIdAndIsDeletedFalse(billId);
        return participants.stream()
                .map(billParticipantMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getBillsByParticipant(String userId) {
        log.debug("Getting bills by participant: {}", userId);

        List<BillParticipant> participants = billParticipantRepository.findByUserIdAndIsDeletedFalse(userId);
        return participants.stream()
                .map(BillParticipant::getBill)
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BillParticipantDto markParticipantAsPaid(String billId, String userId, LocalDateTime paidAt) {
        log.info("Marking participant as paid in bill: {} user: {} at {}", billId, userId, paidAt);

        BillParticipant participant = billParticipantRepository.findByBillIdAndUserId(billId, userId)
                .orElseThrow(() -> EntityNotFoundException.billParticipantNotFound(billId, userId));

        participant.markAsPaid(paidAt);
        participant = billParticipantRepository.save(participant);

        log.info("Participant marked as paid successfully in bill: {}", billId);
        return billParticipantMapStruct.toDto(participant);
    }

    @Override
    public BillParticipantDto markParticipantAsPaid(String billId, String userId) {
        return markParticipantAsPaid(billId, userId, LocalDateTime.now());
    }

    @Override
    public BillParticipantDto markParticipantAsUnpaid(String billId, String userId) {
        log.info("Marking participant as unpaid in bill: {} user: {}", billId, userId);

        BillParticipant participant = billParticipantRepository.findByBillIdAndUserId(billId, userId)
                .orElseThrow(() -> EntityNotFoundException.billParticipantNotFound(billId, userId));

        participant.markAsUnpaid();
        participant = billParticipantRepository.save(participant);

        log.info("Participant marked as unpaid successfully in bill: {}", billId);
        return billParticipantMapStruct.toDto(participant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillSummaryDto> getBillSummaryByNexId(String nexId) {
        log.debug("Getting bill summary by nex ID: {}", nexId);

        List<Bill> bills = billRepository.findByNexIdAndIsDeletedFalse(nexId);
        return bills.stream()
                .map(billMapStruct::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillSummaryDto> getBillSummaryByUserId(String userId) {
        log.debug("Getting bill summary by user ID: {}", userId);

        List<Bill> bills = billRepository.findByCreatedByAndIsDeletedFalse(userId);
        return bills.stream()
                .map(billMapStruct::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateUserOwedAmount(String billId, String userId) {
        log.debug("Calculating user owed amount for bill: {} user: {}", billId, userId);

        BillParticipant participant = billParticipantRepository.findByBillIdAndUserId(billId, userId)
                .orElseThrow(() -> EntityNotFoundException.billParticipantNotFound(billId, userId));

        return participant.getShareAmount();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateUserPaidAmount(String billId, String userId) {
        log.debug("Calculating user paid amount for bill: {} user: {}", billId, userId);

        BillParticipant participant = billParticipantRepository.findByBillIdAndUserId(billId, userId)
                .orElseThrow(() -> EntityNotFoundException.billParticipantNotFound(billId, userId));

        return participant.isPaid() ? participant.getShareAmount() : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BillStatistics getBillStatisticsByNexId(String nexId) {
        log.debug("Getting bill statistics by nex ID: {}", nexId);

        List<Bill> bills = billRepository.findByNexIdAndIsDeletedFalse(nexId);
        return calculateBillStatistics(bills);
    }

    @Override
    @Transactional(readOnly = true)
    public BillStatistics getBillStatisticsByUserId(String userId) {
        log.debug("Getting bill statistics by user ID: {}", userId);

        List<Bill> bills = billRepository.findByCreatedByAndIsDeletedFalse(userId);
        return calculateBillStatistics(bills);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getAllBills() {
        log.debug("Getting all bills");

        List<Bill> bills = billRepository.findByIsDeletedFalse();
        return bills.stream()
                .map(billMapStruct::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillDto> getAllBills(Pageable pageable) {
        log.debug("Getting all bills with pagination");

        Page<Bill> bills = billRepository.findByIsDeletedFalse(pageable);
        return bills.map(billMapStruct::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillDto> getBillsByUserId(String userId, Pageable pageable) {
        log.debug("Getting bills by user ID with pagination: {}", userId);

        Page<Bill> bills = billRepository.findByCreatedByAndIsDeletedFalse(userId, pageable);
        return bills.map(billMapStruct::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillDto> getBillsByNexId(String nexId, Pageable pageable) {
        log.debug("Getting bills by nex ID with pagination: {}", nexId);

        Page<Bill> bills = billRepository.findByNexIdAndIsDeletedFalse(nexId, pageable);
        return bills.map(billMapStruct::toDto);
    }

    @Override
    public Object getBillAnalytics(String nexId) {
        log.debug("Getting bill analytics for nex: {}", nexId);

        // TODO: Implement comprehensive bill analytics
        // This could include trends, spending patterns, etc.
        return "Bill analytics not yet implemented";
    }

    // Helper methods
    private boolean shouldCreateNewBill(Bill bill) {
        // TODO: Implement logic to determine if a new recurring bill should be created
        // This would check the frequency and last created date
        return false;
    }

    private void createNewRecurringBill(Bill originalBill) {
        // TODO: Implement logic to create a new bill based on the recurring pattern
        log.info("Creating new recurring bill based on: {}", originalBill.getId());
    }

    private BillStatistics calculateBillStatistics(List<Bill> bills) {
        int totalBills = bills.size();
        int paidBills = (int) bills.stream().filter(bill -> Boolean.TRUE.equals(bill.getIsPaid())).count();
        int unpaidBills = totalBills - paidBills;
        int overdueBills = (int) bills.stream().filter(this::isOverdue).count();

        BigDecimal totalBillAmount = bills.stream()
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaidAmount = bills.stream()
                .filter(bill -> Boolean.TRUE.equals(bill.getIsPaid()))
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnpaidAmount = totalBillAmount.subtract(totalPaidAmount);
        BigDecimal totalOverdueAmount = bills.stream()
                .filter(this::isOverdue)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate participant statistics
        int totalParticipants = 0;
        int paidParticipants = 0;
        int unpaidParticipants = 0;

        for (Bill bill : bills) {
            List<BillParticipant> participants = billParticipantRepository.findByBillIdAndIsDeletedFalse(bill.getId());
            totalParticipants += participants.size();
            paidParticipants += (int) participants.stream().filter(BillParticipant::isPaid).count();
            unpaidParticipants += participants.size()
                    - (int) participants.stream().filter(BillParticipant::isPaid).count();
        }

        return new BillStatistics(
                totalBills, paidBills, unpaidBills, overdueBills,
                totalBillAmount, totalPaidAmount, totalUnpaidAmount, totalOverdueAmount,
                totalParticipants, paidParticipants, unpaidParticipants);
    }

    private boolean isOverdue(Bill bill) {
        return !Boolean.TRUE.equals(bill.getIsPaid()) && bill.getDueDate() != null
                && bill.getDueDate().isBefore(LocalDateTime.now());
    }
}
