package com.nexsplit.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OAuth2 token exchange requests
 * Used by mobile apps and web apps that handle OAuth2 on the frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2TokenRequest {

    @NotBlank(message = "Google access token is required")
    private String googleToken;
}
