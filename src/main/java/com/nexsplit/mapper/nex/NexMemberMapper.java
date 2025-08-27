package com.nexsplit.mapper.nex;

import com.nexsplit.dto.nex.NexMemberDto;
import com.nexsplit.model.NexMember;
import org.springframework.stereotype.Component;

@Component
public class NexMemberMapper {

    public NexMemberDto toDto(NexMember nexMember) {
        if (nexMember == null || nexMember.getUser() == null) {
            return null;
        }

        return NexMemberDto.builder()
                .userId(nexMember.getUser().getId())
                .username(nexMember.getUser().getUsername())
                .firstName(nexMember.getUser().getFirstName())
                .lastName(nexMember.getUser().getLastName())
                .email(nexMember.getUser().getEmail())
                .role(nexMember.getRole())
                .status(nexMember.getStatus())
                .invitedAt(nexMember.getInvitedAt())
                .joinedAt(nexMember.getJoinedAt())
                .createdAt(nexMember.getCreatedAt())
                .modifiedAt(nexMember.getModifiedAt())
                .build();
    }
}
