package com.nexsplit.dto.nex;

import com.nexsplit.model.Nex;
import com.nexsplit.model.NexMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDto {

    private String nexId;
    private String nexName;
    private String nexDescription;
    private Nex.NexType nexType;
    private Nex.SettlementType settlementType;
    private String nexImageUrl;

    // Inviter information
    private String inviterId;
    private String inviterName;
    private String inviterUsername;
    private String inviterEmail;

    // Invitation details
    private NexMember.MemberRole invitedRole;
    private LocalDateTime invitedAt;
    private String invitationMessage;

    // Nex creator information
    private String creatorId;
    private String creatorName;
    private String creatorUsername;
}
