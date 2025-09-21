package com.nexsplit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for test email operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEmailResponse {
    private boolean success;
    private String message;
    private String email;
    private String timestamp;
}
