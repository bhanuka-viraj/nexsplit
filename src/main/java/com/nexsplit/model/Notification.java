package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Notification entity representing user notifications.
 * 
 * This entity stores various types of notifications for users including
 * invites, reminders, and informational messages. Notifications are
 * associated with users and optionally with specific nex groups.
 * 
 * Database table: notifications
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "notifications")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "nex_id", columnDefinition = "CHAR(36)")
    private String nexId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nex_id", insertable = false, updatable = false)
    private Nex nex;

    /**
     * Enumeration of notification types.
     * 
     * INVITE: Invitation to join a nex group
     * REMINDER: Reminder about bills, expenses, or settlements
     * INFO: General informational notifications
     */
    public enum NotificationType {
        INVITE, REMINDER, INFO
    }

    @PrePersist
    protected void onCreate() {
        if (isRead == null) {
            isRead = false;
        }
    }

    /**
     * Mark the notification as read.
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Mark the notification as unread.
     */
    public void markAsUnread() {
        this.isRead = false;
    }

    /**
     * Check if the notification is read.
     * 
     * @return true if the notification is read, false otherwise
     */
    public boolean isRead() {
        return Boolean.TRUE.equals(this.isRead);
    }
}
