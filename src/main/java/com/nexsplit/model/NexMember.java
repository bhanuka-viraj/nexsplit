package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "nex_members")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NexMember extends BaseEntity {

    @EmbeddedId
    private NexMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("nexId")
    @JoinColumn(name = "nex_id")
    private Nex nex;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    public enum MemberRole {
        ADMIN, MEMBER
    }

    public enum MemberStatus {
        PENDING, ACTIVE, LEFT
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = MemberStatus.ACTIVE;
        }
        if (role == null) {
            role = MemberRole.MEMBER;
        }
    }
}
