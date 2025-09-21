package com.nexsplit.mapper.nex;

import com.nexsplit.dto.nex.InvitationDto;
import com.nexsplit.model.NexMember;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between NexMember entities and Invitation
 * DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between NexMember entities and their corresponding Invitation
 * DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvitationMapStruct {

    /**
     * Convert NexMember entity to InvitationDto.
     * 
     * @param nexMember The nex member entity
     * @return InvitationDto
     */
    @Mapping(target = "nexId", source = "id.nexId")
    @Mapping(target = "nexName", source = "nex.name")
    @Mapping(target = "nexDescription", source = "nex.description")
    @Mapping(target = "nexType", source = "nex.nexType")
    @Mapping(target = "settlementType", source = "nex.settlementType")
    @Mapping(target = "nexImageUrl", source = "nex.imageUrl")
    @Mapping(target = "invitedRole", source = "role")
    @Mapping(target = "invitedAt", source = "invitedAt")
    @Mapping(target = "creatorId", source = "nex.createdBy")
    @Mapping(target = "creatorName", ignore = true) // TODO: Add creator name mapping
    @Mapping(target = "creatorUsername", ignore = true) // TODO: Add creator username mapping
    @Mapping(target = "inviterId", ignore = true) // TODO: Add inviter information
    @Mapping(target = "inviterName", ignore = true) // TODO: Add inviter information
    @Mapping(target = "inviterUsername", ignore = true) // TODO: Add inviter information
    @Mapping(target = "inviterEmail", ignore = true) // TODO: Add inviter information
    @Mapping(target = "invitationMessage", ignore = true) // TODO: Add invitation message
    InvitationDto toDto(NexMember nexMember);

    /**
     * Convert list of NexMember entities to list of InvitationDto.
     * 
     * @param nexMembers List of nex member entities
     * @return List of InvitationDto
     */
    List<InvitationDto> toDtoList(List<NexMember> nexMembers);
}
