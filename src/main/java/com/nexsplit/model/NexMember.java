package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "nex_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NexMember {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

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
        createdAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
        if (status == null) {
            status = MemberStatus.ACTIVE;
        }
        if (role == null) {
            role = MemberRole.MEMBER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
