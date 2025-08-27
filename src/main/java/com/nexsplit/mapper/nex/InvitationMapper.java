package com.nexsplit.mapper.nex;

import com.nexsplit.dto.nex.InvitationDto;
import com.nexsplit.model.Nex;
import com.nexsplit.model.NexMember;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public InvitationDto toInvitationDto(NexMember nexMember) {
        if (nexMember == null || nexMember.getNex() == null || nexMember.getUser() == null) {
            return null;
        }

        Nex nex = nexMember.getNex();

        return InvitationDto.builder()
                .nexId(nex.getId())
                .nexName(nex.getName())
                .nexDescription(nex.getDescription())
                .nexType(nex.getNexType())
                .settlementType(nex.getSettlementType())
                .nexImageUrl(nex.getImageUrl())

                // Inviter information (the person who sent the invitation)
                .inviterId(nex.getCreatedBy()) // For now, assuming creator is inviter
                .inviterName(nex.getCreator() != null
                        ? nex.getCreator().getFirstName() + " " + nex.getCreator().getLastName()
                        : null)
                .inviterUsername(nex.getCreator() != null ? nex.getCreator().getUsername() : null)
                .inviterEmail(nex.getCreator() != null ? nex.getCreator().getEmail() : null)

                // Invitation details
                .invitedRole(nexMember.getRole())
                .invitedAt(nexMember.getInvitedAt())
                .invitationMessage("You've been invited to join this expense group") // Default message

                // Nex creator information
                .creatorId(nex.getCreatedBy())
                .creatorName(nex.getCreator() != null
                        ? nex.getCreator().getFirstName() + " " + nex.getCreator().getLastName()
                        : null)
                .creatorUsername(nex.getCreator() != null ? nex.getCreator().getUsername() : null)
                .build();
    }
}
