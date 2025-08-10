package com.nexsplit.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {
    @NotBlank(message = "Reset token is mandatory")
    private String resetToken;

    @NotBlank(message = "New password is mandatory")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm password is mandatory")
    private String confirmPassword;
}
