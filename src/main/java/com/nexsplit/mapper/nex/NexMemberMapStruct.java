package com.nexsplit.mapper.nex;

import com.nexsplit.dto.nex.NexMemberDto;
import com.nexsplit.dto.nex.NexMemberSummaryDto;
import com.nexsplit.model.NexMember;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between NexMember entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between NexMember entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NexMemberMapStruct {

    /**
     * Convert NexMember entity to NexMemberDto.
     * 
     * @param nexMember The nex member entity
     * @return NexMemberDto
     */
    @Mapping(target = "nexId", source = "id.nexId")
    @Mapping(target = "userId", source = "id.userId")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "nexName", source = "nex.name")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    NexMemberDto toDto(NexMember nexMember);

    /**
     * Convert NexMember entity to NexMemberSummaryDto.
     * 
     * @param nexMember The nex member entity
     * @return NexMemberSummaryDto
     */
    @Mapping(target = "nexId", source = "id.nexId")
    @Mapping(target = "userId", source = "id.userId")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "nexName", source = "nex.name")
    NexMemberSummaryDto toSummaryDto(NexMember nexMember);

    /**
     * Convert list of NexMember entities to list of NexMemberDto.
     * 
     * @param nexMembers List of nex member entities
     * @return List of NexMemberDto
     */
    List<NexMemberDto> toDtoList(List<NexMember> nexMembers);

    /**
     * Convert list of NexMember entities to list of NexMemberSummaryDto.
     * 
     * @param nexMembers List of nex member entities
     * @return List of NexMemberSummaryDto
     */
    List<NexMemberSummaryDto> toSummaryDtoList(List<NexMember> nexMembers);
}
