package com.nexsplit.service.impl;

import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.expense.CreateExpenseRequest;
import com.nexsplit.dto.expense.ExpenseDto;
import com.nexsplit.dto.expense.ExpenseFilter;
import com.nexsplit.model.view.ExpenseSummaryView;
import com.nexsplit.dto.expense.UpdateExpenseRequest;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.mapper.expense.ExpenseMapStruct;
import com.nexsplit.model.*;
import com.nexsplit.repository.*;
import com.nexsplit.repository.ExpenseSummaryRepository;
import com.nexsplit.service.ExpenseService;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.extern.slf4j.Slf4j;
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
        private final ExpenseMapStruct expenseMapStruct;
        private final ExpenseSummaryRepository expenseSummaryRepository;

        public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                        SplitRepository splitRepository,
                        DebtRepository debtRepository,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository,
                        NexMemberRepository nexMemberRepository,
                        ExpenseMapStruct expenseMapStruct,
                        ExpenseSummaryRepository expenseSummaryRepository) {
                this.expenseRepository = expenseRepository;
                this.splitRepository = splitRepository;
                this.debtRepository = debtRepository;
                this.categoryRepository = categoryRepository;
                this.userRepository = userRepository;
                this.nexMemberRepository = nexMemberRepository;
                this.expenseMapStruct = expenseMapStruct;
                this.expenseSummaryRepository = expenseSummaryRepository;
        }

        @Override
        @Transactional
        public ExpenseDto createExpense(CreateExpenseRequest request, String userId) {
                log.info("Creating expense for user: {}", userId);

                // Validate user exists
                userRepository.findById(userId)
                                .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));

                // Validate nex exists and user is member
                if (!isNexMember(request.getNexId(), userId)) {
                        throw new BusinessException("User is not a member of this expense group",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                }

                // Validate category exists
                categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> EntityNotFoundException.categoryNotFound(request.getCategoryId()));

                // Validate payer exists and is member of nex
                userRepository.findById(request.getPayerId())
                                .orElseThrow(() -> EntityNotFoundException.userNotFound(request.getPayerId()));

                if (!isNexMember(request.getNexId(), request.getPayerId())) {
                        throw new BusinessException("Payer is not a member of this expense group",
                                        com.nexsplit.dto.ErrorCode.EXPENSE_PAYER_NOT_MEMBER);
                }

                // Create expense entity
                Expense expense = expenseMapStruct.toEntity(request);
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
                return expenseMapStruct.toDto(savedExpense);
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

                return expenseMapStruct.toDto(expense);
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
                        boolean isMember = isNexMember(filter.getNexId(), userId);
                        log.info("Authorization check for user {} in nex {} (via filter): isMember={}", userId,
                                        filter.getNexId(), isMember);

                        if (!isMember) {
                                log.warn("Access denied: User {} is not a member of nex {} (via filter)", userId,
                                                filter.getNexId());
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
                                .map(expenseMapStruct::toDto)
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
                boolean isMember = isNexMember(nexId, userId);
                log.info("Authorization check for user {} in nex {}: isMember={}", userId, nexId, isMember);

                if (!isMember) {
                        log.warn("Access denied: User {} is not a member of nex {}", userId, nexId);
                        throw new BusinessException("User is not a member of this expense group",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                }

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Expense> expensePage = expenseRepository.findByNexIdAndIsDeletedFalse(nexId, pageable);

                List<ExpenseDto> expenseDtos = expensePage.getContent().stream()
                                .map(expenseMapStruct::toDto)
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
                                .map(expenseMapStruct::toDto)
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
                expenseMapStruct.updateEntityFromRequest(request, expense);

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
                return expenseMapStruct.toDto(updatedExpense);
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
        public ExpenseSummaryView getExpenseSummary(String nexId, String userId) {
                log.info("Getting expense summary for nex: {} by user: {}", nexId, userId);

                // Check if user is member of nex
                if (!isNexMember(nexId, userId)) {
                        throw new BusinessException("User is not a member of this expense group",
                                        com.nexsplit.dto.ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                }

                // Get expense summary from database view
                List<ExpenseSummaryView> summaries = expenseSummaryRepository.findByNexId(nexId);

                // For now, return the first summary or create a basic one
                // In a real implementation, you might want to aggregate multiple summaries
                if (!summaries.isEmpty()) {
                        return summaries.get(0);
                }

                // Return null if no summary found
                return null;
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
                                .map(expenseMapStruct::toDto)
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
                                .map(expenseMapStruct::toDto)
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
                                .map(expenseMapStruct::toDto)
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
                return isNexMember(expense.getNexId(), userId);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean canModifyExpense(String expenseId, String userId) {
                Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId).orElse(null);
                if (expense == null) {
                        return false;
                }

                // Check if user is the creator or an admin of the nex
                return expense.getCreatedBy().equals(userId) || isNexAdmin(expense.getNexId(), userId);
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

                // Validate that total split amounts equal expense amount (accounting for
                // rounding)
                BigDecimal totalSplitAmount = splits.stream()
                                .map(Split::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal difference = totalSplitAmount.subtract(expense.getAmount()).abs();
                if (difference.compareTo(BigDecimal.valueOf(0.01)) > 0) { // Allow 1 cent difference for rounding
                        throw new BusinessException("Split amounts do not equal expense amount. Total: " +
                                        totalSplitAmount + ", Expense: " + expense.getAmount(),
                                        com.nexsplit.dto.ErrorCode.EXPENSE_SPLIT_INVALID);
                }

                // Save splits
                return splitRepository.saveAll(splits);
        }

        /**
         * Check if user is a member of the nex group.
         * 
         * @param nexId  The nex ID
         * @param userId The user ID
         * @return true if user is an active member
         */
        private boolean isNexMember(String nexId, String userId) {
                log.debug("Checking if user {} is member of nex {}", userId, nexId);

                Optional<com.nexsplit.model.NexMember> nexMemberOpt = nexMemberRepository.findByNexIdAndUserId(nexId,
                                userId);

                if (nexMemberOpt.isEmpty()) {
                        log.debug("User {} is not found as member of nex {}", userId, nexId);
                        return false;
                }

                com.nexsplit.model.NexMember nexMember = nexMemberOpt.get();
                boolean isActive = nexMember.getStatus() == com.nexsplit.model.NexMember.MemberStatus.ACTIVE;
                boolean isNotDeleted = !nexMember.isDeleted();
                boolean isMember = isActive && isNotDeleted;

                log.debug("User {} membership status for nex {}: active={}, notDeleted={}, isMember={}",
                                userId, nexId, isActive, isNotDeleted, isMember);

                return isMember;
        }

        /**
         * Check if user is an admin of the nex group.
         * 
         * @param nexId  The nex ID
         * @param userId The user ID
         * @return true if user is an admin
         */
        private boolean isNexAdmin(String nexId, String userId) {
                return nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                                .map(nexMember -> nexMember.getRole() == com.nexsplit.model.NexMember.MemberRole.ADMIN
                                                && nexMember.getStatus() == com.nexsplit.model.NexMember.MemberStatus.ACTIVE
                                                && !nexMember.isDeleted())
                                .orElse(false);
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
                        // Fetch the user entity to set the relationship
                        User user = userRepository.findById(memberId)
                                        .orElseThrow(() -> EntityNotFoundException.userNotFound(memberId));

                        SplitId splitId = SplitId.builder()
                                        .expenseId(expense.getId())
                                        .userId(memberId)
                                        .build();

                        Split split = Split.builder()
                                        .id(splitId)
                                        .percentage(percentagePerPerson)
                                        .amount(amountPerPerson)
                                        .expense(expense)
                                        .user(user)
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
                        // Fetch the user entity to set the relationship
                        User user = userRepository.findById(request.getUserId())
                                        .orElseThrow(() -> EntityNotFoundException.userNotFound(request.getUserId()));

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
                                        .user(user)
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
                        // Fetch the user entity to set the relationship
                        User user = userRepository.findById(request.getUserId())
                                        .orElseThrow(() -> EntityNotFoundException.userNotFound(request.getUserId()));

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
                                        .user(user)
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
