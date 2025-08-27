package com.nexsplit.service.impl;

import com.nexsplit.dto.ErrorCode;
import com.nexsplit.dto.nex.InviteMemberRequest;
import com.nexsplit.dto.nex.InvitationDto;
import com.nexsplit.dto.nex.NexMemberDto;
import com.nexsplit.dto.nex.UpdateMemberRoleRequest;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.mapper.nex.InvitationMapper;
import com.nexsplit.mapper.nex.NexMemberMapper;
import com.nexsplit.model.Nex;
import com.nexsplit.model.NexMember;
import com.nexsplit.model.NexMemberId;
import com.nexsplit.model.User;
import com.nexsplit.repository.NexMemberRepository;
import com.nexsplit.repository.NexRepository;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.service.NexMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final NexMemberMapper nexMemberMapper;
    private final InvitationMapper invitationMapper;

    @Override
    @Transactional
    public void inviteMember(String nexId, InviteMemberRequest request, String inviterId) {
        log.info("Inviting member to nex: {} by user: {}", nexId, inviterId);

        // Validate nex exists and inviter is admin
        Nex nex = nexRepository.findById(nexId)
                .orElseThrow(() -> new BusinessException("Nex not found", ErrorCode.NEX_NOT_FOUND));

        // Check if inviter is admin
        if (!isAdmin(nexId, inviterId)) {
            throw new BusinessException("Only admins can invite members", ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
        }

        // Find user by email
        User userToInvite = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found", ErrorCode.USER_NOT_FOUND));

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
                .invitedAt(LocalDateTime.now())
                .build();

        nexMemberRepository.save(member);

        log.info("Member invited successfully: {} to nex: {}", userToInvite.getId(), nexId);
    }

    @Override
    @Transactional
    public void acceptInvitation(String nexId, String userId) {
        log.info("Accepting invitation for user: {} to nex: {}", userId, nexId);

        NexMember member = nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .orElseThrow(() -> new BusinessException("Invitation not found", ErrorCode.NEX_NOT_FOUND));

        if (member.getStatus() != NexMember.MemberStatus.PENDING) {
            throw new BusinessException("No pending invitation found", ErrorCode.NEX_NOT_FOUND);
        }

        member.setStatus(NexMember.MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());
        nexMemberRepository.save(member);

        log.info("Invitation accepted successfully for user: {} to nex: {}", userId, nexId);
    }

    @Override
    @Transactional
    public void declineInvitation(String nexId, String userId) {
        log.info("Declining invitation for user: {} to nex: {}", userId, nexId);

        NexMember member = nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .orElseThrow(() -> new BusinessException("Invitation not found", ErrorCode.NEX_NOT_FOUND));

        if (member.getStatus() != NexMember.MemberStatus.PENDING) {
            throw new BusinessException("No pending invitation found", ErrorCode.NEX_NOT_FOUND);
        }

        nexMemberRepository.delete(member);

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
                .orElseThrow(() -> new BusinessException("Member not found", ErrorCode.NEX_NOT_MEMBER));

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
                .orElseThrow(() -> new BusinessException("Member not found", ErrorCode.NEX_NOT_MEMBER));

        nexMemberRepository.delete(member);

        log.info("Member removed successfully: {} from nex: {}", memberId, nexId);
    }

    @Override
    @Transactional
    public void leaveNex(String nexId, String userId) {
        log.info("User: {} leaving nex: {}", userId, nexId);

        NexMember member = nexMemberRepository.findByNexIdAndUserId(nexId, userId)
                .orElseThrow(() -> new BusinessException("Member not found", ErrorCode.NEX_NOT_MEMBER));

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
    public List<NexMemberDto> getNexMembers(String nexId, String userId) {
        log.info("Getting members for nex: {} by user: {}", nexId, userId);

        // Check if user is member
        if (!isMember(nexId, userId)) {
            throw new BusinessException("Access denied", ErrorCode.AUTHZ_NEX_ACCESS_DENIED);
        }

        List<NexMember> members = nexMemberRepository.findAllMembersByNexId(nexId);
        return members.stream()
                .map(nexMemberMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationDto> getPendingInvitations(String userId) {
        log.info("Getting pending invitations for user: {}", userId);

        List<NexMember> pendingInvitations = nexMemberRepository.findPendingInvitationsByUserId(userId);
        return pendingInvitations.stream()
                .map(invitationMapper::toInvitationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NexMemberDto> getUserMemberships(String userId) {
        log.info("Getting memberships for user: {}", userId);

        List<NexMember> memberships = nexMemberRepository.findActiveMembershipsByUserId(userId);
        return memberships.stream()
                .map(nexMemberMapper::toDto)
                .collect(Collectors.toList());
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
