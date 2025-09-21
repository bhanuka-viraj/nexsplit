package com.nexsplit.dto.nex;

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
public class NexMemberDto {

    private String nexId;
    private String userId;
    private String userName;
    private String userEmail;
    private String nexName;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private NexMember.MemberRole role;
    private NexMember.MemberStatus status;
    private LocalDateTime invitedAt;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
