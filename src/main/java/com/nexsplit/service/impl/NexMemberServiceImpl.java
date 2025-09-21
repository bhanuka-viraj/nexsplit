package com.nexsplit.service.impl;

import com.nexsplit.dto.ErrorCode;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.nex.InviteMemberRequest;
import com.nexsplit.dto.nex.InvitationDto;
import com.nexsplit.dto.nex.NexMemberDto;
import com.nexsplit.dto.nex.UpdateMemberRoleRequest;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.exception.EntityNotFoundException;
import com.nexsplit.mapper.nex.NexMemberMapStruct;
import com.nexsplit.mapper.nex.InvitationMapStruct;
import com.nexsplit.model.Nex;
import com.nexsplit.model.NexMember;
import com.nexsplit.model.NexMemberId;
import com.nexsplit.model.User;
import com.nexsplit.repository.NexMemberRepository;
import com.nexsplit.repository.NexRepository;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.service.EventService;
import com.nexsplit.service.NexMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NexMemberServiceImpl implements NexMemberService {

    private final NexMemberRepository nexMemberRepository;
    private final NexRepository nexRepository;
    private final UserRepository userRepository;
    private final NexMemberMapStruct nexMemberMapStruct;
    private final InvitationMapStruct invitationMapStruct;
    private final EventService eventService;

    @Override
    @Transactional
    public void inviteMember(String nexId, InviteMemberRequest request, String inviterId) {
        log.info("Inviting member to nex: {} by user: {}", nexId, inviterId);

        // Validate nex exists and inviter is admin
        Nex nex = nexRepository.findById(nexId)
                .orElseThrow(() -> EntityNotFoundException.nexNotFound(nexId));

        // Check if inviter is admin
        if (!isAdmin(nexId, inviterId)) {
            throw new BusinessException("Only admins can invite members", ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
        }

        // Find user by email
        User userToInvite = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found with email: " + request.getEmail(),
                        ErrorCode.USER_NOT_FOUND));

        // Check if user is already a member
        Optional<NexMember> existingMember = nexMemberRepository.findByNexIdAndUserId(nexId, userToInvite.getId());
        if (existingMember.isPresent()) {
            throw new BusinessException("User is already a member of this nex", ErrorCode.NEX_ALREADY_MEMBER);
        }

        // Create member invitation
        NexMemberId memberId = NexMemberId.builder()
                .nexId(nexId)
                .userId(userToInvite.getId())
                .build();

        NexMember member = NexMember.builder()
                .id(memberId)
                .nex(nex)
                .user(userToInvite)
                .role(request.getRole())
                .status(NexMember.MemberStatus.PENDING)
                .isDeleted(false)
                .invitedAt(LocalDateTime.now())
                .build();

        nexMemberRepository.save(member);

        // Broadcast invitation sent event
        eventService.broadcastInvitationSent(nexId, userToInvite.getId(), inviterId);

        log.info("Member invited successfully: {} to nex: {}", userToInvite.getId(), nexId);
    }

    @Override
    @Transactional
    public void acceptInvitation(String nexId, String userId) {
        log.info("Accepting invitation for user: {} to nex: {}", userId, nexId);

        NexMember member = nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .orElseThrow(() -> new BusinessException("No pending invitation found for this user",
                        ErrorCode.NEX_NOT_FOUND));

        if (member.getStatus() != NexMember.MemberStatus.PENDING) {
            throw new BusinessException("No pending invitation found for this user", ErrorCode.NEX_NOT_FOUND);
        }

        member.setStatus(NexMember.MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());
        nexMemberRepository.save(member);

        // Broadcast invitation accepted event
        eventService.broadcastInvitationAccepted(nexId, userId);

        log.info("Invitation accepted successfully for user: {} to nex: {}", userId, nexId);
    }

    @Override
    @Transactional
    public void declineInvitation(String nexId, String userId) {
        log.info("Declining invitation for user: {} to nex: {}", userId, nexId);

        NexMember member = nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .orElseThrow(() -> new BusinessException("No pending invitation found for this user",
                        ErrorCode.NEX_NOT_FOUND));

        if (member.getStatus() != NexMember.MemberStatus.PENDING) {
            throw new BusinessException("No pending invitation found for this user", ErrorCode.NEX_NOT_FOUND);
        }

        nexMemberRepository.delete(member);

        // Broadcast invitation declined event
        eventService.broadcastInvitationDeclined(nexId, userId);

        log.info("Invitation declined successfully for user: {} to nex: {}", userId, nexId);
    }

    @Override
    @Transactional
    public void updateMemberRole(String nexId, String memberId, UpdateMemberRoleRequest request, String adminId) {
        log.info("Updating member role for user: {} in nex: {} by admin: {}", memberId, nexId, adminId);

        // Check if admin is admin
        if (!isAdmin(nexId, adminId)) {
            throw new BusinessException("Only admins can update member roles",
                    ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
        }

        NexMember member = nexMemberRepository.findByNexIdAndUserId(nexId, memberId)
                .orElseThrow(() -> new BusinessException("Member not found in this expense group",
                        ErrorCode.NEX_NOT_MEMBER));

        member.setRole(request.getRole());
        nexMemberRepository.save(member);

        log.info("Member role updated successfully for user: {} in nex: {}", memberId, nexId);
    }

    @Override
    @Transactional
    public void removeMember(String nexId, String memberId, String adminId) {
        log.info("Removing member: {} from nex: {} by admin: {}", memberId, nexId, adminId);

        // Check if admin is admin
        if (!isAdmin(nexId, adminId)) {
            throw new BusinessException("Only admins can remove members", ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
        }

        // Cannot remove yourself as admin if you're the only admin
        if (adminId.equals(memberId)) {
            List<NexMember> admins = nexMemberRepository.findAdminsByNexId(nexId);
            if (admins.size() == 1) {
                throw new BusinessException("Cannot remove the only admin from the nex",
                        ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
            }
        }

        NexMember member = nexMemberRepository.findByNexIdAndUserId(nexId, memberId)
                .orElseThrow(() -> new BusinessException("Member not found in this expense group",
                        ErrorCode.NEX_NOT_MEMBER));

        nexMemberRepository.delete(member);

        log.info("Member removed successfully: {} from nex: {}", memberId, nexId);
    }

    @Override
    @Transactional
    public void leaveNex(String nexId, String userId) {
        log.info("User: {} leaving nex: {}", userId, nexId);

        NexMember member = nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .orElseThrow(() -> new BusinessException("Member not found in this expense group",
                        ErrorCode.NEX_NOT_MEMBER));

        // If user is admin and only admin, prevent leaving
        if (member.getRole() == NexMember.MemberRole.ADMIN) {
            List<NexMember> admins = nexMemberRepository.findAdminsByNexId(nexId);
            if (admins.size() == 1) {
                throw new BusinessException("Cannot leave as the only admin. Please transfer admin role first.",
                        ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
            }
        }

        member.setStatus(NexMember.MemberStatus.LEFT);
        nexMemberRepository.save(member);

        log.info("User left nex successfully: {} from nex: {}", userId, nexId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<NexMemberDto> getNexMembers(String nexId, String userId, int page, int size) {
        log.info("Getting paginated members for nex: {} by user: {}, page: {}, size: {}", nexId, userId, page, size);

        // Check if user is member
        if (!isMember(nexId, userId)) {
            throw new BusinessException("Access denied", ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<NexMember> memberPage = nexMemberRepository.findAllMembersByNexIdPaginated(nexId, pageable);

        List<NexMemberDto> memberDtos = memberPage.getContent().stream()
                .map(nexMemberMapStruct::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<NexMemberDto>builder()
                .data(memberDtos)
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(memberPage.getTotalElements())
                        .totalPages(memberPage.getTotalPages())
                        .hasNext(memberPage.hasNext())
                        .hasPrevious(memberPage.hasPrevious())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InvitationDto> getPendingInvitations(String userId, int page, int size) {
        log.info("Getting paginated pending invitations for user: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NexMember> invitationPage = nexMemberRepository.findPendingInvitationsByUserIdPaginated(userId, pageable);

        List<InvitationDto> invitationDtos = invitationPage.getContent().stream()
                .map(invitationMapStruct::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<InvitationDto>builder()
                .data(invitationDtos)
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(invitationPage.getTotalElements())
                        .totalPages(invitationPage.getTotalPages())
                        .hasNext(invitationPage.hasNext())
                        .hasPrevious(invitationPage.hasPrevious())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<NexMemberDto> getUserMemberships(String userId, int page, int size) {
        log.info("Getting paginated memberships for user: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NexMember> membershipPage = nexMemberRepository.findActiveMembershipsByUserIdPaginated(userId, pageable);

        List<NexMemberDto> membershipDtos = membershipPage.getContent().stream()
                .map(nexMemberMapStruct::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<NexMemberDto>builder()
                .data(membershipDtos)
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalElements(membershipPage.getTotalElements())
                        .totalPages(membershipPage.getTotalPages())
                        .hasNext(membershipPage.hasNext())
                        .hasPrevious(membershipPage.hasPrevious())
                        .build())
                .build();
    }

    private boolean isMember(String nexId, String userId) {
        Optional<NexMember> member = nexMemberRepository.findByNexIdAndUserId(nexId, userId);
        return member.isPresent() && member.get().getStatus() == NexMember.MemberStatus.ACTIVE;
    }

    private boolean isAdmin(String nexId, String userId) {
        Optional<NexMember> member = nexMemberRepository.findByNexIdAndUserId(nexId, userId);
        return member.isPresent() &&
                member.get().getStatus() == NexMember.MemberStatus.ACTIVE &&
                member.get().getRole() == NexMember.MemberRole.ADMIN;
    }
}
