package com.nexsplit.mapper.user;

import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Centralized registry for all user-related mappers
 * Provides a single point of access for all user mapping operations
 */
@Component
@RequiredArgsConstructor
public class UserMapperRegistry {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UpdateUserMapper updateUserMapper;

    // UserMapper methods
    public User toEntity(UserDto userDto) {
        return userMapper.toEntity(userDto);
    }

    public UserDto toDto(User user) {
        return userMapper.toDto(user);
    }

    public User updateEntityFromDto(User user, UserDto userDto) {
        return userMapper.updateEntityFromDto(user, userDto);
    }

    // UserProfileMapper methods
    public UserProfileDto toProfileDto(User user) {
        return userProfileMapper.toDto(user);
    }

    public User updateEntityFromProfileDto(User user, UserProfileDto userProfileDto) {
        return userProfileMapper.updateEntityFromDto(user, userProfileDto);
    }

    public UserProfileDto toUpdateProfileDto(User user) {
        return userProfileMapper.toUpdateDto(user);
    }

    // UpdateUserMapper methods
    public User updateEntityFromUpdateDto(User user, UpdateUserDto updateUserDto) {
        return updateUserMapper.updateEntityFromDto(user, updateUserDto);
    }

    public UpdateUserDto toUpdateDto(User user) {
        return updateUserMapper.toDto(user);
    }

    public UpdateUserDto toChangedFieldsDto(User originalUser, User updatedUser) {
        return updateUserMapper.toChangedFieldsDto(originalUser, updatedUser);
    }

    /**
     * Convenience method to create a UserProfileDto from User entity
     * 
     * @param user the user entity
     * @return UserProfileDto
     */
    public UserProfileDto fromUser(User user) {
        return userProfileMapper.toDto(user);
    }
}
