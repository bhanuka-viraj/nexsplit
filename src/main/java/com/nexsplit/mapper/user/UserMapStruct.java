package com.nexsplit.mapper.user;

import com.nexsplit.dto.user.CreateUserRequest;
import com.nexsplit.dto.user.UpdateUserRequest;
import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.dto.user.UserSummaryDto;
import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.model.User;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between User entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between User entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapStruct {

    /**
     * Convert CreateUserRequest to User entity.
     * 
     * @param request The create request DTO
     * @return User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "lastValidationCode", ignore = true)
    @Mapping(target = "isEmailValidate", ignore = true)
    @Mapping(target = "isGoogleAuth", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    User toEntity(CreateUserRequest request);

    /**
     * Convert UserDto to User entity.
     * 
     * @param userDto The user DTO
     * @return User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "lastValidationCode", ignore = true)
    @Mapping(target = "isEmailValidate", ignore = true)
    @Mapping(target = "isGoogleAuth", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    User toEntity(UserDto userDto);

    /**
     * Convert User entity to UserDto.
     * 
     * @param user The user entity
     * @return UserDto
     */
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "lastValidationCode", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    UserDto toDto(User user);

    /**
     * Convert User entity to UserSummaryDto.
     * 
     * @param user The user entity
     * @return UserSummaryDto
     */
    UserSummaryDto toSummaryDto(User user);

    /**
     * Convert User entity to UserProfileDto.
     * 
     * @param user The user entity
     * @return UserProfileDto
     */
    UserProfileDto toProfileDto(User user);

    /**
     * Convert UpdateUserDto to UpdateUserRequest.
     * 
     * @param updateUserDto The update user DTO
     * @return UpdateUserRequest
     */
    UpdateUserRequest toUpdateUserRequest(UpdateUserDto updateUserDto);

    /**
     * Update User entity from UpdateUserRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request The update request DTO
     * @param user    The user entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "lastValidationCode", ignore = true)
    @Mapping(target = "isEmailValidate", ignore = true)
    @Mapping(target = "isGoogleAuth", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);

    /**
     * Convert list of User entities to list of UserDto.
     * 
     * @param users List of user entities
     * @return List of UserDto
     */
    List<UserDto> toDtoList(List<User> users);

    /**
     * Convert list of User entities to list of UserSummaryDto.
     * 
     * @param users List of user entities
     * @return List of UserSummaryDto
     */
    List<UserSummaryDto> toSummaryDtoList(List<User> users);
}
