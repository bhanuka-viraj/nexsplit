package com.nexsplit.service.impl;

import com.nexsplit.dto.debt.CreateDebtRequest;
import com.nexsplit.dto.debt.DebtDto;
import com.nexsplit.dto.debt.DebtSummaryDto;
import com.nexsplit.dto.debt.UpdateDebtRequest;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.model.view.SettlementHistoryView;
import com.nexsplit.mapper.debt.DebtMapStruct;
import com.nexsplit.model.Debt;
import com.nexsplit.repository.DebtRepository;
import com.nexsplit.repository.SettlementRepository;
import com.nexsplit.repository.NexMemberRepository;
import com.nexsplit.service.DebtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Implementation of DebtService for debt management operations.
 * 
 * This service provides comprehensive debt management functionality including
 * debt creation, settlement tracking, and analytics using database views
 * for optimal performance.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;
    private final SettlementRepository settlementRepository;
    private final NexMemberRepository nexMemberRepository;
    private final DebtMapStruct debtMapStruct;

    @Override
    public DebtDto createDebt(CreateDebtRequest request) {
        log.info("Creating debt for debtor: {} and creditor: {}", request.getDebtorId(), request.getCreditorId());

        Debt debt = debtMapStruct.toEntity(request);
        Debt savedDebt = debtRepository.save(debt);

        log.info("Successfully created debt with ID: {}", savedDebt.getId());
        return debtMapStruct.toDto(savedDebt);
    }

    @Override
    public DebtDto updateDebt(String debtId, UpdateDebtRequest request) {
        log.info("Updating debt with ID: {}", debtId);

        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> EntityNotFoundException.debtNotFound(debtId));

        debtMapStruct.updateEntityFromRequest(request, debt);
        Debt updatedDebt = debtRepository.save(debt);

        log.info("Successfully updated debt with ID: {}", debtId);
        return debtMapStruct.toDto(updatedDebt);
    }

    @Override
    public void deleteDebt(String debtId, String deletedBy) {
        log.info("Deleting debt with ID: {} by user: {}", debtId, deletedBy);

        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> EntityNotFoundException.debtNotFound(debtId));

        debt.softDelete(deletedBy);
        debtRepository.save(debt);

        log.info("Successfully deleted debt with ID: {}", debtId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtDto> getDebtsByUserId(String userId) {
        log.debug("Retrieving debts for user ID: {}", userId);

        List<SettlementHistoryView> settlements = settlementRepository.findByUserId(userId);
        return settlements.stream()
                .map(this::convertSettlementToDebtDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtDto> getDebtsByNexId(String nexId) {
        log.debug("Retrieving debts for nex ID: {}", nexId);

        List<SettlementHistoryView> settlements = settlementRepository.findByNexId(nexId);
        return settlements.stream()
                .map(this::convertSettlementToDebtDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtDto> getUnsettledDebtsByUserId(String userId) {
        log.debug("Retrieving unsettled debts for user ID: {}", userId);

        List<SettlementHistoryView> settlements = settlementRepository.findUnsettledByUserId(userId);
        return settlements.stream()
                .map(this::convertSettlementToDebtDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtDto> getUnsettledDebtsByNexId(String nexId) {
        log.debug("Retrieving unsettled debts for nex ID: {}", nexId);

        List<SettlementHistoryView> settlements = settlementRepository.findUnsettledByNexId(nexId);
        return settlements.stream()
                .map(this::convertSettlementToDebtDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtDto> getDebtsBetweenUsers(String userId1, String userId2) {
        log.debug("Retrieving debts between users: {} and {}", userId1, userId2);

        List<SettlementHistoryView> settlements = settlementRepository.findBetweenUsers(userId1, userId2);
        return settlements.stream()
                .map(this::convertSettlementToDebtDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtDto> getDebtsByExpenseId(String expenseId) {
        log.debug("Retrieving debts for expense ID: {}", expenseId);

        List<SettlementHistoryView> settlements = settlementRepository.findByExpenseId(expenseId);
        return settlements.stream()
                .map(this::convertSettlementToDebtDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementHistoryView> getSettlementHistoryByUserId(String userId) {
        log.debug("Retrieving settlement history for user ID: {}", userId);

        return settlementRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementHistoryView> getSettlementHistoryByNexId(String nexId) {
        log.debug("Retrieving settlement history for nex ID: {}", nexId);

        return settlementRepository.findByNexId(nexId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementHistoryView> getSettlementHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Retrieving settlement history between {} and {}", startDate, endDate);

        return settlementRepository.findByDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Object[] getSettlementStatisticsByUserId(String userId) {
        log.debug("Retrieving settlement statistics for user ID: {}", userId);

        return settlementRepository.getSettlementStatistics(userId)
                .orElse(new Object[] { 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0.0 });
    }

    @Override
    @Transactional(readOnly = true)
    public Object[] getSettlementStatisticsByNexId(String nexId) {
        log.debug("Retrieving settlement statistics for nex ID: {}", nexId);

        return settlementRepository.getSettlementStatisticsByNex(nexId)
                .orElse(new Object[] { 0, 0, 0, BigDecimal.ZERO, 0.0 });
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateUserBalance(String userId) {
        log.debug("Calculating balance for user ID: {}", userId);

        Object[] stats = getSettlementStatisticsByUserId(userId);
        BigDecimal totalCredit = (BigDecimal) stats[4]; // total_credit
        BigDecimal totalDebt = (BigDecimal) stats[3]; // total_debt

        return totalCredit.subtract(totalDebt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtDto> getDebtsByPaymentMethod(String paymentMethod) {
        log.debug("Retrieving debts by payment method: {}", paymentMethod);

        List<SettlementHistoryView> settlements = settlementRepository.findByPaymentMethod(paymentMethod);
        return settlements.stream()
                .map(this::convertSettlementToDebtDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtSummaryDto> getDebtSummaryByUserId(String userId) {
        log.debug("Retrieving debt summary for user ID: {}", userId);

        List<SettlementHistoryView> settlements = settlementRepository.findByUserId(userId);
        return settlements.stream()
                .map(this::convertSettlementToDebtSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtSummaryDto> getDebtSummaryByNexId(String nexId) {
        log.debug("Retrieving debt summary for nex ID: {}", nexId);

        List<SettlementHistoryView> settlements = settlementRepository.findByNexId(nexId);
        return settlements.stream()
                .map(this::convertSettlementToDebtSummaryDto)
                .toList();
    }

    /**
     * Convert SettlementHistoryView to DebtDto.
     * 
     * @param settlement The settlement history DTO
     * @return Debt DTO
     */
    private DebtDto convertSettlementToDebtDto(SettlementHistoryView settlement) {
        return DebtDto.builder()
                .id(settlement.getDebtId())
                .debtorId(settlement.getDebtorId())
                .debtorName(settlement.getDebtorName())
                .debtorEmail(settlement.getDebtorEmail())
                .creditorId(settlement.getCreditorId())
                .creditorName(settlement.getCreditorName())
                .creditorEmail(settlement.getCreditorEmail())
                .creditorType(Debt.CreditorType.valueOf(settlement.getCreditorType()))
                .amount(settlement.getAmount())
                .expenseId(settlement.getExpenseId())
                .expenseTitle(settlement.getExpenseTitle())
                .paymentMethod(settlement.getPaymentMethod())
                .notes(settlement.getDebtNotes())
                .settledAt(settlement.getSettledAt())
                .createdAt(settlement.getDebtCreatedAt())
                .modifiedAt(settlement.getDebtModifiedAt())
                .isSettled(settlement.getIsSettled())
                .build();
    }

    /**
     * Convert SettlementHistoryView to DebtSummaryDto.
     * 
     * @param settlement The settlement history DTO
     * @return Debt summary DTO
     */
    private DebtSummaryDto convertSettlementToDebtSummaryDto(SettlementHistoryView settlement) {
        return DebtSummaryDto.builder()
                .id(settlement.getDebtId())
                .debtorId(settlement.getDebtorId())
                .debtorName(settlement.getDebtorName())
                .creditorId(settlement.getCreditorId())
                .creditorName(settlement.getCreditorName())
                .creditorType(Debt.CreditorType.valueOf(settlement.getCreditorType()))
                .amount(settlement.getAmount())
                .expenseId(settlement.getExpenseId())
                .expenseTitle(settlement.getExpenseTitle())
                .paymentMethod(settlement.getPaymentMethod())
                .settledAt(settlement.getSettledAt())
                .createdAt(settlement.getDebtCreatedAt())
                .isSettled(settlement.getIsSettled())
                .build();
    }

    @Override
    public DebtDto getDebtById(String debtId) {
        log.debug("Getting debt by ID: {}", debtId);

        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> EntityNotFoundException.debtNotFound(debtId));

        return debtMapStruct.toDto(debt);
    }

    @Override
    public void deleteDebt(String debtId) {
        log.info("Deleting debt: {}", debtId);

        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> EntityNotFoundException.debtNotFound(debtId));

        debtRepository.delete(debt);
    }

    @Override
    public Page<DebtDto> getDebtsByUserId(String userId, Pageable pageable) {
        log.debug("Getting debts by user ID: {} with pagination: {}", userId, pageable);

        Page<SettlementHistoryView> settlements = settlementRepository.findByUserIdPaginated(userId, pageable);
        return settlements.map(this::convertSettlementToDebtDto);
    }

    @Override
    public Page<DebtDto> getDebtsByNexId(String nexId, Pageable pageable) {
        log.debug("Getting debts by nex ID: {} with pagination: {}", nexId, pageable);

        Page<SettlementHistoryView> settlements = settlementRepository.findByNexIdPaginated(nexId, pageable);
        return settlements.map(this::convertSettlementToDebtDto);
    }

    @Override
    public Page<DebtDto> getAllDebts(Pageable pageable) {
        log.debug("Getting all debts with pagination: {}", pageable);

        Page<SettlementHistoryView> settlements = settlementRepository.findAllPaginated(pageable);
        return settlements.map(this::convertSettlementToDebtDto);
    }

    @Override
    public BigDecimal getUserBalance(String userId) {
        log.debug("Getting balance for user: {}", userId);

        return calculateUserBalance(userId);
    }

    @Override
    public List<DebtSummaryDto> getNexBalances(String nexId) {
        log.debug("Getting balances for nex: {}", nexId);

        // Get all active member IDs in the nex group
        List<String> memberIds = nexMemberRepository.findActiveMemberIdsByNexId(nexId);

        if (memberIds.isEmpty()) {
            log.warn("No active members found for nex: {}", nexId);
            return List.of();
        }

        // Calculate balance for each member
        List<DebtSummaryDto> balances = new ArrayList<>();

        for (String memberId : memberIds) {
            BigDecimal balance = calculateUserBalance(memberId);

            // Only include members with non-zero balances
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                DebtSummaryDto balanceDto = DebtSummaryDto.builder()
                        .id("balance_" + memberId + "_" + nexId)
                        .debtorId(balance.compareTo(BigDecimal.ZERO) < 0 ? memberId : null)
                        .creditorId(balance.compareTo(BigDecimal.ZERO) > 0 ? memberId : null)
                        .amount(balance.abs())
                        .isSettled(false)
                        .createdAt(LocalDateTime.now())
                        .build();

                balances.add(balanceDto);
            }
        }

        log.debug("Calculated balances for {} members in nex: {}", balances.size(), nexId);
        return balances;
    }
}
