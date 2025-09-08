package com.nexsplit.service.impl;

import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.expense.CreateExpenseRequest;
import com.nexsplit.dto.expense.ExpenseDto;
import com.nexsplit.dto.expense.ExpenseFilter;
import com.nexsplit.dto.expense.ExpenseSummaryDto;
import com.nexsplit.dto.expense.UpdateExpenseRequest;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.mapper.expense.ExpenseMapper;
import com.nexsplit.model.*;
import com.nexsplit.repository.*;
import com.nexsplit.service.ExpenseService;
import com.nexsplit.service.NexService;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for expense management.
 * Handles expense CRUD operations, split calculations, and debt generation.
 */
@Service
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

        private final ExpenseRepository expenseRepository;
        private final SplitRepository splitRepository;
        private final DebtRepository debtRepository;
        private final CategoryRepository categoryRepository;
        private final UserRepository userRepository;
        private final NexMemberRepository nexMemberRepository;
        private final NexService nexService;
        private final ExpenseMapper expenseMapper;

        public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                        SplitRepository splitRepository,
                        DebtRepository debtRepository,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository,
                        NexMemberRepository nexMemberRepository,
                        @Lazy NexService nexService,
                        ExpenseMapper expenseMapper) {
                this.expenseRepository = expenseRepository;
                this.splitRepository = splitRepository;
                this.debtRepository = debtRepository;
                this.categoryRepository = categoryRepository;
                this.userRepository = userRepository;
                this.nexMemberRepository = nexMemberRepository;
                this.nexService = nexService;
                this.expenseMapper = expenseMapper;
        }

        @Override
        @Transactional
        public ExpenseDto createExpense(CreateExpenseRequest request, String userId) {
                log.info("Creating expense for user: {}", userId);

                // Validate user exists
                userRepository.findById(userId)
                                .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));

                // Validate nex exists and user is member
                if (!nexService.isMember(request.getNexId(), userId)) {
                        throw new BusinessException("User is not a member of this expense group",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                }

                // Validate category exists
                categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> EntityNotFoundException.categoryNotFound(request.getCategoryId()));

                // Validate payer exists and is member of nex
                userRepository.findById(request.getPayerId())
                                .orElseThrow(() -> EntityNotFoundException.userNotFound(request.getPayerId()));

                if (!nexService.isMember(request.getNexId(), request.getPayerId())) {
                        throw new BusinessException("Payer is not a member of this expense group",
                                        com.nexsplit.dto.ErrorCode.EXPENSE_PAYER_NOT_MEMBER);
                }

                // Create expense entity
                Expense expense = expenseMapper.toEntity(request);
                expense.setCreatedBy(userId);
                expense.setIsDeleted(false);

                // Save expense
                Expense savedExpense = expenseRepository.save(expense);

                // Calculate and create splits
                List<Split> splits = calculateAndCreateSplits(savedExpense, request.getSplits(), request.getNexId());
                savedExpense.setSplits(splits);

                // Generate debts from splits
                List<Debt> debts = generateDebtsFromSplits(savedExpense, splits);
                savedExpense.setDebts(debts);

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "EXPENSE_CREATED",
                                userId,
                                "CREATE_EXPENSE",
                                "SUCCESS",
                                Map.of(
                                                "expenseId", savedExpense.getId(),
                                                "nexId", request.getNexId(),
                                                "amount", request.getAmount(),
                                                "currency", request.getCurrency(),
                                                "splitType", request.getSplitType().name(),
                                                "splitCount", splits.size()));

                log.info("Expense created successfully: {}", savedExpense.getId());
                return expenseMapper.toDto(savedExpense);
        }

        @Override
        @Transactional(readOnly = true)
        public ExpenseDto getExpenseById(String expenseId, String userId) {
                log.info("Getting expense: {} for user: {}", expenseId, userId);

                Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId)
                                .orElseThrow(() -> EntityNotFoundException.expenseNotFound(expenseId));

                // Check if user has access to this expense
                if (!hasAccessToExpense(expenseId, userId)) {
                        throw new BusinessException("Access denied to this expense",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                }

                return expenseMapper.toDto(expense);
        }

        @Override
        @Transactional(readOnly = true)
        public PaginatedResponse<ExpenseDto> getExpenses(ExpenseFilter filter, String userId, int page, int size) {
                log.info("Getting expenses with filter for user: {}, page: {}, size: {}", userId, page, size);

                // Create pageable with sorting
                Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<Expense> expensePage;

                // Apply filters
                if (filter.getNexId() != null) {
                        // Check if user is member of nex
                        if (!nexService.isMember(filter.getNexId(), userId)) {
                                throw new BusinessException("User is not a member of this expense group",
                                                com.nexsplit.dto.ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                        }
                        expensePage = expenseRepository.findByNexIdAndIsDeletedFalse(filter.getNexId(), pageable);
                } else if (filter.getUserId() != null) {
                        expensePage = expenseRepository.findExpensesByUserInvolvement(filter.getUserId(), pageable);
                } else {
                        // Get all expenses where user is involved
                        expensePage = expenseRepository.findExpensesByUserInvolvement(userId, pageable);
                }

                List<ExpenseDto> expenseDtos = expensePage.getContent().stream()
                                .map(expenseMapper::toDto)
                                .collect(Collectors.toList());

                return PaginatedResponse.<ExpenseDto>builder()
                                .data(expenseDtos)
                                .pagination(PaginatedResponse.PaginationInfo.builder()
                                                .page(page)
                                                .size(size)
                                                .totalElements(expensePage.getTotalElements())
                                                .totalPages(expensePage.getTotalPages())
                                                .hasNext(expensePage.hasNext())
                                                .hasPrevious(expensePage.hasPrevious())
                                                .build())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PaginatedResponse<ExpenseDto> getExpensesByNexId(String nexId, String userId, int page, int size) {
                log.info("Getting expenses for nex: {} by user: {}, page: {}, size: {}", nexId, userId, page, size);

                // Check if user is member of nex
                if (!nexService.isMember(nexId, userId)) {
                        throw new BusinessException("User is not a member of this expense group",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                }

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Expense> expensePage = expenseRepository.findByNexIdAndIsDeletedFalse(nexId, pageable);

                List<ExpenseDto> expenseDtos = expensePage.getContent().stream()
                                .map(expenseMapper::toDto)
                                .collect(Collectors.toList());

                return PaginatedResponse.<ExpenseDto>builder()
                                .data(expenseDtos)
                                .pagination(PaginatedResponse.PaginationInfo.builder()
                                                .page(page)
                                                .size(size)
                                                .totalElements(expensePage.getTotalElements())
                                                .totalPages(expensePage.getTotalPages())
                                                .hasNext(expensePage.hasNext())
                                                .hasPrevious(expensePage.hasPrevious())
                                                .build())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PaginatedResponse<ExpenseDto> getExpensesByUserInvolvement(String userId, int page, int size) {
                log.info("Getting expenses by user involvement: {}, page: {}, size: {}", userId, page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Expense> expensePage = expenseRepository.findExpensesByUserInvolvement(userId, pageable);

                List<ExpenseDto> expenseDtos = expensePage.getContent().stream()
                                .map(expenseMapper::toDto)
                                .collect(Collectors.toList());

                return PaginatedResponse.<ExpenseDto>builder()
                                .data(expenseDtos)
                                .pagination(PaginatedResponse.PaginationInfo.builder()
                                                .page(page)
                                                .size(size)
                                                .totalElements(expensePage.getTotalElements())
                                                .totalPages(expensePage.getTotalPages())
                                                .hasNext(expensePage.hasNext())
                                                .hasPrevious(expensePage.hasPrevious())
                                                .build())
                                .build();
        }

        @Override
        @Transactional
        public ExpenseDto updateExpense(String expenseId, UpdateExpenseRequest request, String userId) {
                log.info("Updating expense: {} by user: {}", expenseId, userId);

                Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId)
                                .orElseThrow(() -> EntityNotFoundException.expenseNotFound(expenseId));

                // Check if user can modify this expense
                if (!canModifyExpense(expenseId, userId)) {
                        throw new BusinessException("User cannot modify this expense",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
                }

                // Update expense fields
                expenseMapper.updateEntityFromRequest(request, expense);

                // If splits are provided, recalculate them
                if (request.getSplits() != null) {
                        // Delete existing splits and debts
                        splitRepository.deleteByIdExpenseId(expenseId);
                        debtRepository.deleteByExpenseId(expenseId);

                        // Create new splits
                        List<Split> newSplits = calculateAndCreateSplits(expense, request.getSplits(),
                                        expense.getNexId());
                        expense.setSplits(newSplits);

                        // Generate new debts
                        List<Debt> newDebts = generateDebtsFromSplits(expense, newSplits);
                        expense.setDebts(newDebts);
                }

                Expense updatedExpense = expenseRepository.save(expense);

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "EXPENSE_UPDATED",
                                userId,
                                "UPDATE_EXPENSE",
                                "SUCCESS",
                                Map.of("expenseId", expenseId, "nexId", expense.getNexId()));

                log.info("Expense updated successfully: {}", expenseId);
                return expenseMapper.toDto(updatedExpense);
        }

        @Override
        @Transactional
        public void deleteExpense(String expenseId, String userId) {
                log.info("Soft deleting expense: {} by user: {}", expenseId, userId);

                Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId)
                                .orElseThrow(() -> EntityNotFoundException.expenseNotFound(expenseId));

                // Check if user can modify this expense
                if (!canModifyExpense(expenseId, userId)) {
                        throw new BusinessException("User cannot delete this expense",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
                }

                // Soft delete the expense
                expenseRepository.softDeleteById(expenseId, userId);

                // Log business event
                StructuredLoggingUtil.logBusinessEvent(
                                "EXPENSE_DELETED",
                                userId,
                                "DELETE_EXPENSE",
                                "SUCCESS",
                                Map.of("expenseId", expenseId, "nexId", expense.getNexId()));

                log.info("Expense soft deleted successfully: {}", expenseId);
        }

        @Override
        @Transactional(readOnly = true)
        public ExpenseSummaryDto getExpenseSummary(String nexId, String userId) {
                log.info("Getting expense summary for nex: {} by user: {}", nexId, userId);

                // Check if user is member of nex
                if (!nexService.isMember(nexId, userId)) {
                        throw new BusinessException("User is not a member of this expense group",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                }

                // Get total amount and count
                BigDecimal totalAmount = expenseRepository.calculateTotalAmountByNexId(nexId);
                long totalCount = expenseRepository.countByNexIdAndIsDeletedFalse(nexId);

                // Get unsettled debt amount
                List<Debt> unsettledDebts = debtRepository.findUnsettledByNexId(nexId);
                BigDecimal unsettledAmount = unsettledDebts.stream()
                                .map(Debt::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return ExpenseSummaryDto.builder()
                                .totalExpenses((int) totalCount)
                                .totalExpenseAmount(totalAmount)
                                .unsettledAmount(unsettledAmount)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PaginatedResponse<ExpenseDto> searchExpenses(String searchTerm, String userId, int page, int size) {
                log.info("Searching expenses with term: '{}' by user: {}, page: {}, size: {}", searchTerm, userId, page,
                                size);

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Expense> expensePage = expenseRepository.searchExpensesByTitleOrDescription(searchTerm, pageable);

                // Filter expenses where user has access
                List<ExpenseDto> expenseDtos = expensePage.getContent().stream()
                                .filter(expense -> hasAccessToExpense(expense.getId(), userId))
                                .map(expenseMapper::toDto)
                                .collect(Collectors.toList());

                return PaginatedResponse.<ExpenseDto>builder()
                                .data(expenseDtos)
                                .pagination(PaginatedResponse.PaginationInfo.builder()
                                                .page(page)
                                                .size(size)
                                                .totalElements(expensePage.getTotalElements())
                                                .totalPages(expensePage.getTotalPages())
                                                .hasNext(expensePage.hasNext())
                                                .hasPrevious(expensePage.hasPrevious())
                                                .build())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PaginatedResponse<ExpenseDto> getExpensesByCategoryId(String categoryId, String userId, int page,
                        int size) {
                log.info("Getting expenses by category: {} for user: {}, page: {}, size: {}", categoryId, userId, page,
                                size);

                // Validate category exists
                categoryRepository.findById(categoryId)
                                .orElseThrow(() -> EntityNotFoundException.categoryNotFound(categoryId));
                List<Expense> expenses = expenseRepository
                                .findByCategoryIdAndIsDeletedFalseOrderByCreatedAtDesc(categoryId);

                // Filter expenses where user has access
                List<ExpenseDto> expenseDtos = expenses.stream()
                                .filter(expense -> hasAccessToExpense(expense.getId(), userId))
                                .skip((long) page * size)
                                .limit(size)
                                .map(expenseMapper::toDto)
                                .collect(Collectors.toList());

                return PaginatedResponse.<ExpenseDto>builder()
                                .data(expenseDtos)
                                .pagination(PaginatedResponse.PaginationInfo.builder()
                                                .page(page)
                                                .size(size)
                                                .totalElements(expenses.size())
                                                .totalPages((int) Math.ceil((double) expenses.size() / size))
                                                .hasNext(page < (int) Math.ceil((double) expenses.size() / size) - 1)
                                                .hasPrevious(page > 0)
                                                .build())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PaginatedResponse<ExpenseDto> getExpensesByPayerId(String payerId, String userId, int page, int size) {
                log.info("Getting expenses by payer: {} for user: {}, page: {}, size: {}", payerId, userId, page, size);

                List<Expense> expenses = expenseRepository.findByPayerIdAndIsDeletedFalseOrderByCreatedAtDesc(payerId);

                // Filter expenses where user has access
                List<ExpenseDto> expenseDtos = expenses.stream()
                                .filter(expense -> hasAccessToExpense(expense.getId(), userId))
                                .skip((long) page * size)
                                .limit(size)
                                .map(expenseMapper::toDto)
                                .collect(Collectors.toList());

                return PaginatedResponse.<ExpenseDto>builder()
                                .data(expenseDtos)
                                .pagination(PaginatedResponse.PaginationInfo.builder()
                                                .page(page)
                                                .size(size)
                                                .totalElements(expenses.size())
                                                .totalPages((int) Math.ceil((double) expenses.size() / size))
                                                .hasNext(page < (int) Math.ceil((double) expenses.size() / size) - 1)
                                                .hasPrevious(page > 0)
                                                .build())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public boolean hasAccessToExpense(String expenseId, String userId) {
                Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId).orElse(null);
                if (expense == null) {
                        return false;
                }

                // Check if user is member of the nex
                return nexService.isMember(expense.getNexId(), userId);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean canModifyExpense(String expenseId, String userId) {
                Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId).orElse(null);
                if (expense == null) {
                        return false;
                }

                // Check if user is the creator or an admin of the nex
                return expense.getCreatedBy().equals(userId) || nexService.isAdmin(expense.getNexId(), userId);
        }

        /**
         * Calculate and create splits for an expense.
         * 
         * @param expense       The expense entity
         * @param splitRequests List of split requests
         * @param nexId         The nex ID
         * @return List of created splits
         */
        private List<Split> calculateAndCreateSplits(Expense expense, List<?> splitRequests, String nexId) {
                List<Split> splits = new ArrayList<>();

                if (expense.getSplitType() == Expense.SplitType.EQUALLY) {
                        // Get all active members of the nex
                        List<String> memberIds = getNexMemberIds(nexId);
                        splits = createEqualSplits(expense, memberIds);
                } else if (expense.getSplitType() == Expense.SplitType.PERCENTAGE) {
                        @SuppressWarnings("unchecked")
                        List<CreateExpenseRequest.CreateSplitRequest> percentageSplits = (List<CreateExpenseRequest.CreateSplitRequest>) splitRequests;
                        splits = createPercentageSplits(expense, percentageSplits);
                } else if (expense.getSplitType() == Expense.SplitType.AMOUNT) {
                        @SuppressWarnings("unchecked")
                        List<CreateExpenseRequest.CreateSplitRequest> amountSplits = (List<CreateExpenseRequest.CreateSplitRequest>) splitRequests;
                        splits = createAmountSplits(expense, amountSplits);
                }

                // Save splits
                return splitRepository.saveAll(splits);
        }

        /**
         * Create equal splits for all nex members.
         */
        private List<Split> createEqualSplits(Expense expense, List<String> memberIds) {
                List<Split> splits = new ArrayList<>();
                BigDecimal totalAmount = expense.getAmount();
                int memberCount = memberIds.size();

                if (memberCount == 0) {
                        throw new BusinessException("No members found in expense group",
                                        com.nexsplit.dto.ErrorCode.EXPENSE_SPLIT_INVALID);
                }

                BigDecimal amountPerPerson = totalAmount.divide(BigDecimal.valueOf(memberCount), 2,
                                RoundingMode.HALF_UP);
                BigDecimal percentagePerPerson = BigDecimal.valueOf(100).divide(BigDecimal.valueOf(memberCount), 2,
                                RoundingMode.HALF_UP);

                for (String memberId : memberIds) {
                        SplitId splitId = SplitId.builder()
                                        .expenseId(expense.getId())
                                        .userId(memberId)
                                        .build();

                        Split split = Split.builder()
                                        .id(splitId)
                                        .percentage(percentagePerPerson)
                                        .amount(amountPerPerson)
                                        .expense(expense)
                                        .build();

                        splits.add(split);
                }

                return splits;
        }

        /**
         * Create percentage-based splits.
         */
        private List<Split> createPercentageSplits(Expense expense,
                        List<CreateExpenseRequest.CreateSplitRequest> splitRequests) {
                List<Split> splits = new ArrayList<>();
                BigDecimal totalAmount = expense.getAmount();
                BigDecimal totalPercentage = BigDecimal.ZERO;

                // Validate total percentage
                for (CreateExpenseRequest.CreateSplitRequest request : splitRequests) {
                        totalPercentage = totalPercentage.add(request.getPercentage());
                }

                if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
                        throw new BusinessException("Total percentage must equal 100%",
                                        com.nexsplit.dto.ErrorCode.EXPENSE_SPLIT_INVALID);
                }

                for (CreateExpenseRequest.CreateSplitRequest request : splitRequests) {
                        BigDecimal amount = totalAmount.multiply(request.getPercentage()).divide(
                                        BigDecimal.valueOf(100), 2,
                                        RoundingMode.HALF_UP);

                        SplitId splitId = SplitId.builder()
                                        .expenseId(expense.getId())
                                        .userId(request.getUserId())
                                        .build();

                        Split split = Split.builder()
                                        .id(splitId)
                                        .percentage(request.getPercentage())
                                        .amount(amount)
                                        .notes(request.getNotes())
                                        .expense(expense)
                                        .build();

                        splits.add(split);
                }

                return splits;
        }

        /**
         * Create amount-based splits.
         */
        private List<Split> createAmountSplits(Expense expense,
                        List<CreateExpenseRequest.CreateSplitRequest> splitRequests) {
                List<Split> splits = new ArrayList<>();
                BigDecimal totalAmount = expense.getAmount();
                BigDecimal totalSplitAmount = BigDecimal.ZERO;

                // Validate total amount
                for (CreateExpenseRequest.CreateSplitRequest request : splitRequests) {
                        totalSplitAmount = totalSplitAmount.add(request.getAmount());
                }

                if (totalSplitAmount.compareTo(totalAmount) != 0) {
                        throw new BusinessException("Total split amount must equal expense amount",
                                        com.nexsplit.dto.ErrorCode.EXPENSE_SPLIT_INVALID);
                }

                for (CreateExpenseRequest.CreateSplitRequest request : splitRequests) {
                        BigDecimal percentage = request.getAmount().multiply(BigDecimal.valueOf(100)).divide(
                                        totalAmount, 2,
                                        RoundingMode.HALF_UP);

                        SplitId splitId = SplitId.builder()
                                        .expenseId(expense.getId())
                                        .userId(request.getUserId())
                                        .build();

                        Split split = Split.builder()
                                        .id(splitId)
                                        .percentage(percentage)
                                        .amount(request.getAmount())
                                        .notes(request.getNotes())
                                        .expense(expense)
                                        .build();

                        splits.add(split);
                }

                return splits;
        }

        /**
         * Generate debts from splits.
         */
        private List<Debt> generateDebtsFromSplits(Expense expense, List<Split> splits) {
                List<Debt> debts = new ArrayList<>();
                String payerId = expense.getPayerId();

                for (Split split : splits) {
                        String debtorId = split.getId().getUserId();

                        // Don't create debt if payer is paying for themselves
                        if (!payerId.equals(debtorId)) {
                                Debt debt = Debt.builder()
                                                .debtorId(debtorId)
                                                .creditorId(payerId)
                                                .creditorType(Debt.CreditorType.USER)
                                                .amount(split.getAmount())
                                                .expenseId(expense.getId())
                                                .build();

                                debts.add(debt);
                        }
                }

                return debtRepository.saveAll(debts);
        }

        /**
         * Get member IDs for a nex.
         */
        private List<String> getNexMemberIds(String nexId) {
                return nexMemberRepository.findActiveMemberIdsByNexId(nexId);
        }

        /**
         * Create sort object from sort parameters.
         */
        private Sort createSort(String sortBy, String sortDirection) {
                Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                return Sort.by(direction, sortBy != null ? sortBy : "createdAt");
        }
}
