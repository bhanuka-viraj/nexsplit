package com.nexsplit.mapper.user;

import com.nexsplit.dto.user.UserDto;
import com.nexsplit.model.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for converting between User entity and UserDto
 */
@Component
public class UserMapper {

    /**
     * Converts UserDto to User entity for registration
     * Note: This method doesn't set the password as it should be encoded separately
     * 
     * @param userDto the DTO to convert
     * @return User entity
     */
    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return User.builder()
                .id(userDto.getId() != null ? userDto.getId() : UUID.randomUUID().toString())
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .username(userDto.getUsername())
                .contactNumber(userDto.getContactNumber())
                .isEmailValidate(false)
                .isGoogleAuth(false)
                .status(User.Status.ACTIVE)
                .build();
    }

    /**
     * Converts User entity to UserDto
     * Note: This method doesn't include the password for security reasons
     * 
     * @param user the entity to convert
     * @return UserDto
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setUsername(user.getUsername());
        userDto.setContactNumber(user.getContactNumber());
        // Password is intentionally not set for security reasons

        return userDto;
    }

    /**
     * Updates an existing User entity with data from UserDto
     * Note: This method doesn't update the password as it should be handled
     * separately
     * 
     * @param user    the existing user entity
     * @param userDto the DTO containing updated data
     * @return updated User entity
     */
    public User updateEntityFromDto(User user, UserDto userDto) {
        if (user == null || userDto == null) {
            return user;
        }

        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
        if (userDto.getContactNumber() != null) {
            user.setContactNumber(userDto.getContactNumber());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        return user;
    }
}
