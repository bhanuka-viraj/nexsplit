package com.nexsplit.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password validation response
 * Contains validation result and message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password validation response")
public class PasswordValidationResponse {

    @Schema(description = "Whether the password meets strength requirements", example = "true")
    private boolean valid;

    @Schema(description = "Validation message", example = "Strong password")
    private String message;
}
