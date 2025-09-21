package com.nexsplit.dto.notification;

import com.nexsplit.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notification data.
 * Contains complete notification information.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String nexId;
    private String nexName;
    private Notification.NotificationType type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
