package com.nexsplit.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password validation requests
 * Used for validating password strength before submission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password validation request")
public class PasswordValidationRequest {

    @NotBlank(message = "Password is required")
    @Schema(description = "Password to validate", example = "MySecurePass123!")
    private String password;
}
