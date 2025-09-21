package com.nexsplit.dto.notification;

import com.nexsplit.model.Notification;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing notification.
 * Contains fields that can be modified for a notification.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationRequest {

    private Notification.NotificationType type;

    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    private Boolean isRead;
}
