package com.nexsplit.mapper.user;

import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between User entity and UpdateUserDto
 */
@Component
public class UpdateUserMapper {

    /**
     * Updates an existing User entity with data from UpdateUserDto
     * 
     * @param user          the existing user entity
     * @param updateUserDto the DTO containing updated data
     * @return updated User entity
     */
    public User updateEntityFromDto(User user, UpdateUserDto updateUserDto) {
        if (user == null || updateUserDto == null) {
            return user;
        }

        if (updateUserDto.getFirstName() != null) {
            user.setFirstName(updateUserDto.getFirstName());
        }
        if (updateUserDto.getLastName() != null) {
            user.setLastName(updateUserDto.getLastName());
        }
        if (updateUserDto.getUsername() != null) {
            user.setUsername(updateUserDto.getUsername());
        }
        if (updateUserDto.getContactNumber() != null) {
            user.setContactNumber(updateUserDto.getContactNumber());
        }

        return user;
    }

    /**
     * Converts User entity to UpdateUserDto
     * 
     * @param user the entity to convert
     * @return UpdateUserDto
     */
    public UpdateUserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setFirstName(user.getFirstName());
        updateUserDto.setLastName(user.getLastName());
        updateUserDto.setUsername(user.getUsername());
        updateUserDto.setContactNumber(user.getContactNumber());

        return updateUserDto;
    }

    /**
     * Creates a new UpdateUserDto with only the fields that have changed
     * 
     * @param originalUser the original user entity
     * @param updatedUser  the updated user entity
     * @return UpdateUserDto with only changed fields
     */
    public UpdateUserDto toChangedFieldsDto(User originalUser, User updatedUser) {
        if (originalUser == null || updatedUser == null) {
            return null;
        }

        UpdateUserDto updateUserDto = new UpdateUserDto();

        // Only include fields that have actually changed
        if (!originalUser.getFirstName().equals(updatedUser.getFirstName())) {
            updateUserDto.setFirstName(updatedUser.getFirstName());
        }
        if (!originalUser.getLastName().equals(updatedUser.getLastName())) {
            updateUserDto.setLastName(updatedUser.getLastName());
        }
        if (!originalUser.getUsername().equals(updatedUser.getUsername())) {
            updateUserDto.setUsername(updatedUser.getUsername());
        }
        if (!originalUser.getContactNumber().equals(updatedUser.getContactNumber())) {
            updateUserDto.setContactNumber(updatedUser.getContactNumber());
        }

        return updateUserDto;
    }
}
