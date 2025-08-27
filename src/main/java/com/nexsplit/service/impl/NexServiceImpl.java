package com.nexsplit.service.impl;

import com.nexsplit.dto.nex.CreateNexRequest;
import com.nexsplit.dto.nex.NexDto;
import com.nexsplit.dto.nex.NexSummaryDto;
import com.nexsplit.dto.nex.UpdateNexRequest;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.ErrorCode;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.mapper.nex.NexMapper;
import com.nexsplit.model.Nex;
import com.nexsplit.model.NexMember;
import com.nexsplit.model.NexMemberId;
import com.nexsplit.model.User;
import com.nexsplit.repository.NexMemberRepository;
import com.nexsplit.repository.NexRepository;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.service.NexService;
import com.nexsplit.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NexServiceImpl implements NexService {

    private final NexRepository nexRepository;
    private final NexMemberRepository nexMemberRepository;
    private final UserRepository userRepository;
    private final NexMapper nexMapper;

    @Override
    @Transactional
    public NexDto createNex(CreateNexRequest request, String userId) {
        log.info("Creating nex for user: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", ErrorCode.USER_NOT_FOUND));

        // Create nex
        Nex nex = nexMapper.toEntity(request);
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
                .joinedAt(LocalDateTime.now())
                .build();

        nexMemberRepository.save(creatorMember);

        log.info("Nex created successfully: {}", savedNex.getId());
        return nexMapper.toDto(savedNex);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NexDto> getUserNexes(String userId) {
        log.info("Getting nexes for user: {}", userId);

        List<Nex> nexes = nexRepository.findByMembersUserIdAndStatus(userId);
        return nexes.stream()
                .map(nexMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NexDto getNexById(String nexId, String userId) {
        log.info("Getting nex: {} for user: {}", nexId, userId);

        Nex nex = nexRepository.findByIdAndMembersUserId(nexId, userId)
                .orElseThrow(() -> new BusinessException("Nex not found or access denied", ErrorCode.NEX_NOT_FOUND));

        return nexMapper.toDto(nex);
    }

    @Override
    @Transactional
    public NexDto updateNex(String nexId, UpdateNexRequest request, String userId) {
        log.info("Updating nex: {} by user: {}", nexId, userId);

        // Check if user is admin
        if (!isAdmin(nexId, userId)) {
            throw new BusinessException("Only admins can update nex", ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
        }

        Nex nex = nexRepository.findById(nexId)
                .orElseThrow(() -> new BusinessException("Nex not found", ErrorCode.NEX_NOT_FOUND));

        nexMapper.updateEntityFromRequest(request, nex);
        Nex updatedNex = nexRepository.save(nex);

        log.info("Nex updated successfully: {}", nexId);
        return nexMapper.toDto(updatedNex);
    }

    @Override
    @Transactional
    public void deleteNex(String nexId, String userId) {
        log.info("Deleting nex: {} by user: {}", nexId, userId);

        // Check if user is admin
        if (!isAdmin(nexId, userId)) {
            throw new BusinessException("Only admins can delete nex", ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
        }

        Nex nex = nexRepository.findById(nexId)
                .orElseThrow(() -> new BusinessException("Nex not found", ErrorCode.NEX_NOT_FOUND));

        // Delete all members first (cascade delete)
        nexMemberRepository.deleteByNexId(nexId);

        // Delete the nex
        nexRepository.delete(nex);

        log.info("Nex deleted successfully: {}", nexId);
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

        Nex nex = nexRepository.findById(nexId)
                .orElseThrow(() -> new BusinessException("Nex not found", ErrorCode.NEX_NOT_FOUND));

        // Get member count
        long memberCount = nexMemberRepository.countActiveMembersByNexId(nexId);

        // TODO: Add expense and debt calculations when those services are implemented
        return NexSummaryDto.builder()
                .nexId(nexId)
                .nexName(nex.getName())
                .totalMembers((int) memberCount)
                .totalExpenses(0) // TODO: Implement when ExpenseService is available
                .totalCategories(0) // TODO: Implement when CategoryService is available
                .totalExpenseAmount(BigDecimal.ZERO) // TODO: Implement when ExpenseService is available
                .totalDebtAmount(BigDecimal.ZERO) // TODO: Implement when DebtService is available
                .totalSettledAmount(BigDecimal.ZERO) // TODO: Implement when SettlementService is available
                .pendingSettlements(0) // TODO: Implement when SettlementService is available
                .completedSettlements(0) // TODO: Implement when SettlementService is available
                .build();
    }
}
