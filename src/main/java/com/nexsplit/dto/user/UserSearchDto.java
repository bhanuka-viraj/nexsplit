package com.nexsplit.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user search results.
 * 
 * This DTO contains the essential information needed for user search results,
 * optimized for invitation and user selection scenarios.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {

    /**
     * User's unique identifier
     */
    private String id;

    /**
     * User's email address
     */
    private String email;

    /**
     * User's username
     */
    private String username;

    /**
     * User's first name
     */
    private String firstName;

    /**
     * User's last name
     */
    private String lastName;

    /**
     * User's full name (computed from firstName and lastName)
     */
    private String fullName;

    /**
     * User's contact number
     */
    private String contactNumber;

    /**
     * User's profile picture URL (if available)
     */
    private String profilePictureUrl;

    /**
     * Whether the user has verified their email
     */
    private Boolean isEmailVerified;

    /**
     * Whether the user uses Google authentication
     */
    private Boolean isGoogleAuth;

    /**
     * User's status (ACTIVE, INACTIVE)
     */
    private String status;
}
