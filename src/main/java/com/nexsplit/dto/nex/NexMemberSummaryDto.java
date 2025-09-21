package com.nexsplit.dto.nex;

import com.nexsplit.model.NexMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Summary DTO for nex member information.
 * Contains essential nex member data for list views and summaries.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NexMemberSummaryDto {

    private String nexId;
    private String userId;
    private String userName;
    private String userEmail;
    private String nexName;
    private NexMember.MemberRole role;
    private LocalDateTime invitedAt;
    private LocalDateTime joinedAt;
    private NexMember.MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
