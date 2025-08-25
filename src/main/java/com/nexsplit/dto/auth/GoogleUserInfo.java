package com.nexsplit.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Google user information
 * Contains user details extracted from Google OAuth2 token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {

    private String email;
    private String name;
    private String givenName;
    private String familyName;
    private String picture;
    private String sub; // Google user ID
    private boolean emailVerified;
    private String locale;
    private String hd; // Hosted domain (for Google Workspace users)
}
