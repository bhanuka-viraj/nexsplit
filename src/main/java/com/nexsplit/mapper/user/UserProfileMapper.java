package com.nexsplit.mapper.user;

import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between User entity and UserProfileDto
 */
@Component
public class UserProfileMapper {

    /**
     * Converts User entity to UserProfileDto
     * 
     * @param user the entity to convert
     * @return UserProfileDto
     */
    public UserProfileDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .contactNumber(user.getContactNumber())
                .fullName(user.getFullName())
                .isEmailValidate(user.getIsEmailValidate())
                .isGoogleAuth(user.getIsGoogleAuth())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .modifiedAt(user.getModifiedAt())
                .build();
    }

    /**
     * Updates an existing User entity with data from UserProfileDto
     * Note: This method doesn't update sensitive fields like email, status, or
     * timestamps
     * 
     * @param user           the existing user entity
     * @param userProfileDto the DTO containing updated data
     * @return updated User entity
     */
    public User updateEntityFromDto(User user, UserProfileDto userProfileDto) {
        if (user == null || userProfileDto == null) {
            return user;
        }

        if (userProfileDto.getFirstName() != null) {
            user.setFirstName(userProfileDto.getFirstName());
        }
        if (userProfileDto.getLastName() != null) {
            user.setLastName(userProfileDto.getLastName());
        }
        if (userProfileDto.getUsername() != null) {
            user.setUsername(userProfileDto.getUsername());
        }
        if (userProfileDto.getContactNumber() != null) {
            user.setContactNumber(userProfileDto.getContactNumber());
        }

        return user;
    }

    /**
     * Creates a partial UserProfileDto with only the fields that can be updated
     * 
     * @param user the entity to convert
     * @return UserProfileDto with only updatable fields
     */
    public UserProfileDto toUpdateDto(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .contactNumber(user.getContactNumber())
                .build();
    }
}
