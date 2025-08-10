package com.nexsplit.dto.user;

import com.nexsplit.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String contactNumber;
    private String fullName;
    private Boolean isEmailValidate;
    private Boolean isGoogleAuth;
    private User.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
