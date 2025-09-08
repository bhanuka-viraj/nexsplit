package com.nexsplit.service;

import com.nexsplit.dto.nex.InviteMemberRequest;
import com.nexsplit.dto.nex.InvitationDto;
import com.nexsplit.dto.nex.NexMemberDto;
import com.nexsplit.dto.nex.UpdateMemberRoleRequest;
import com.nexsplit.dto.PaginatedResponse;

public interface NexMemberService {

    /**
     * Invite a user to join a nex group
     */
    void inviteMember(String nexId, InviteMemberRequest request, String inviterId);

    /**
     * Accept an invitation to join a nex group
     */
    void acceptInvitation(String nexId, String userId);

    /**
     * Decline an invitation to join a nex group
     */
    void declineInvitation(String nexId, String userId);

    /**
     * Update member role (admin only)
     */
    void updateMemberRole(String nexId, String memberId, UpdateMemberRoleRequest request, String adminId);

    /**
     * Remove member from nex group (admin only)
     */
    void removeMember(String nexId, String memberId, String adminId);

    /**
     * Leave a nex group
     */
    void leaveNex(String nexId, String userId);

    /**
     * Get paginated members of a nex group
     */
    PaginatedResponse<NexMemberDto> getNexMembers(String nexId, String userId, int page, int size);

    /**
     * Get paginated pending invitations for a user
     */
    PaginatedResponse<InvitationDto> getPendingInvitations(String userId, int page, int size);

    /**
     * Get paginated user's active memberships
     */
    PaginatedResponse<NexMemberDto> getUserMemberships(String userId, int page, int size);
}
