package com.nexsplit.service.impl;

import com.nexsplit.dto.nex.CreateNexRequest;
import com.nexsplit.dto.nex.NexDto;
import com.nexsplit.dto.nex.NexSummaryDto;
import com.nexsplit.dto.nex.UpdateNexRequest;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.expense.ExpenseSummaryDto;
import com.nexsplit.dto.category.CategorySummaryDto;
import com.nexsplit.dto.ErrorCode;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.mapper.nex.NexMapper;
import com.nexsplit.model.Nex;
import com.nexsplit.model.NexMember;
import com.nexsplit.model.NexMemberId;
import com.nexsplit.model.User;
import com.nexsplit.repository.NexMemberRepository;
import com.nexsplit.repository.NexRepository;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.service.NexService;
import com.nexsplit.service.ExpenseService;
import com.nexsplit.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NexServiceImpl implements NexService {

        private final NexRepository nexRepository;
        private final NexMemberRepository nexMemberRepository;
        private final UserRepository userRepository;
        private final NexMapper nexMapper;
        private final ExpenseService expenseService;
        private final CategoryService categoryService;

        public NexServiceImpl(NexRepository nexRepository,
                        NexMemberRepository nexMemberRepository,
                        UserRepository userRepository,
                        NexMapper nexMapper,
                        @Lazy ExpenseService expenseService,
                        @Lazy CategoryService categoryService) {
                this.nexRepository = nexRepository;
                this.nexMemberRepository = nexMemberRepository;
                this.userRepository = userRepository;
                this.nexMapper = nexMapper;
                this.expenseService = expenseService;
                this.categoryService = categoryService;
        }

        @Override
        @Transactional
        public NexDto createNex(CreateNexRequest request, String userId) {
                log.info("Creating nex for user: {}", userId);

                // Validate user exists
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> EntityNotFoundException.userNotFound(userId));

                // Create nex
                Nex nex = nexMapper.toEntity(request);
                nex.setIsArchived(false);
                nex.setIsDeleted(false);
                nex.setCreatedBy(userId);

                Nex savedNex = nexRepository.save(nex);

                // Add creator as admin member
                NexMemberId memberId = NexMemberId.builder()
                                .nexId(savedNex.getId())
                                .userId(userId)
                                .build();

                NexMember creatorMember = NexMember.builder()
                                .id(memberId)
                                .nex(savedNex)
                                .user(user)
                                .role(NexMember.MemberRole.ADMIN)
                                .status(NexMember.MemberStatus.ACTIVE)
                                .isDeleted(false)
                                .joinedAt(java.time.LocalDateTime.now())
                                .build();

                nexMemberRepository.save(creatorMember);

                log.info("Nex created successfully: {}", savedNex.getId());
                return nexMapper.toDto(savedNex);
        }

        @Override
        @Transactional(readOnly = true)
        public NexDto getNexById(String nexId, String userId) {
                log.info("Getting nex: {} for user: {}", nexId, userId);

                Nex nex = nexRepository.findByIdAndMembersUserId(nexId, userId)
                                .orElseThrow(() -> EntityNotFoundException.nexNotFound(nexId));

                return nexMapper.toDto(nex);
        }

        @Override
        @Transactional
        public NexDto updateNex(String nexId, UpdateNexRequest request, String userId) {
                log.info("Updating nex: {} by user: {}", nexId, userId);

                // Check if user is admin
                if (!isAdmin(nexId, userId)) {
                        throw new BusinessException("Only admins can update nex",
                                        ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
                }

                Nex nex = nexRepository.findByIdAndNotDeleted(nexId)
                                .orElseThrow(() -> EntityNotFoundException.nexNotFound(nexId));

                nexMapper.updateEntityFromRequest(request, nex);
                Nex updatedNex = nexRepository.save(nex);

                log.info("Nex updated successfully: {}", nexId);
                return nexMapper.toDto(updatedNex);
        }

        @Override
        @Transactional
        public void deleteNex(String nexId, String userId) {
                log.info("Soft deleting nex: {} by user: {}", nexId, userId);

                // Check if user is admin
                if (!isAdmin(nexId, userId)) {
                        throw new BusinessException("Only admins can delete nex",
                                        ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
                }

                // Check if nex exists and is not deleted
                nexRepository.findByIdAndNotDeleted(nexId)
                                .orElseThrow(() -> EntityNotFoundException.nexNotFound(nexId));

                // Soft delete all members first
                nexMemberRepository.softDeleteByNexId(nexId);

                // Soft delete the nex
                nexRepository.softDeleteById(nexId, userId);

                log.info("Nex soft deleted successfully: {}", nexId);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean isMember(String nexId, String userId) {
                Optional<NexMember> member = nexMemberRepository.findByNexIdAndUserId(nexId, userId);
                return member.isPresent() && member.get().getStatus() == NexMember.MemberStatus.ACTIVE;
        }

        @Override
        @Transactional(readOnly = true)
        public boolean isAdmin(String nexId, String userId) {
                Optional<NexMember> member = nexMemberRepository.findByNexIdAndUserId(nexId, userId);
                return member.isPresent() &&
                                member.get().getStatus() == NexMember.MemberStatus.ACTIVE &&
                                member.get().getRole() == NexMember.MemberRole.ADMIN;
        }

        @Override
        @Transactional(readOnly = true)
        public PaginatedResponse<NexDto> getUserNexesPaginated(String userId, int page, int size) {
                log.info("Getting paginated nexes for user: {}, page: {}, size: {}",
                                userId, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<Nex> nexPage = nexRepository.findByMembersUserId(userId, pageable);

                List<NexDto> nexDtos = nexPage.getContent().stream()
                                .map(nexMapper::toDto)
                                .collect(Collectors.toList());

                return PaginatedResponse.<NexDto>builder()
                                .data(nexDtos)
                                .pagination(PaginatedResponse.PaginationInfo.builder()
                                                .page(page)
                                                .size(size)
                                                .totalElements(nexPage.getTotalElements())
                                                .totalPages(nexPage.getTotalPages())
                                                .hasNext(nexPage.hasNext())
                                                .hasPrevious(nexPage.hasPrevious())
                                                .build())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public NexSummaryDto getNexSummary(String nexId, String userId) {
                log.info("Getting nex summary: {} for user: {}", nexId, userId);

                // Check if user is member
                if (!isMember(nexId, userId)) {
                        throw new BusinessException("Access denied", ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
                }

                Nex nex = nexRepository.findByIdAndNotDeleted(nexId)
                                .orElseThrow(() -> EntityNotFoundException.nexNotFound(nexId));

                // Get member count
                long memberCount = nexMemberRepository.countActiveMembersByNexId(nexId);

                // Get expense summary
                ExpenseSummaryDto expenseSummary = expenseService.getExpenseSummary(nexId, userId);

                // Get category count (we'll get a small page to count)
                PaginatedResponse<CategorySummaryDto> categoriesResponse = categoryService.getCategoriesByNexId(nexId,
                                userId, 0, 1);
                int totalCategories = (int) categoriesResponse.getPagination().getTotalElements();

                return NexSummaryDto.builder()
                                .nexId(nexId)
                                .nexName(nex.getName())
                                .totalMembers((int) memberCount)
                                .totalExpenses(expenseSummary.getTotalExpenses())
                                .totalCategories(totalCategories)
                                .totalExpenseAmount(expenseSummary.getTotalExpenseAmount())
                                .totalDebtAmount(expenseSummary.getUnsettledAmount()) // Using unsettled amount as debt
                                                                                      // amount
                                .totalSettledAmount(expenseSummary.getTotalExpenseAmount()
                                                .subtract(expenseSummary.getUnsettledAmount()))
                                .pendingSettlements(0) // TODO: Implement when SettlementService is available
                                .completedSettlements(0) // TODO: Implement when SettlementService is available
                                .build();
        }
}
