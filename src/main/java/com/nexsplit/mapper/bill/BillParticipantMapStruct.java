package com.nexsplit.mapper.bill;

import com.nexsplit.dto.bill.BillParticipantDto;
import com.nexsplit.dto.bill.BillParticipantSummaryDto;
import com.nexsplit.dto.bill.CreateBillParticipantRequest;
import com.nexsplit.dto.bill.UpdateBillParticipantRequest;
import com.nexsplit.model.BillParticipant;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between BillParticipant entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between BillParticipant entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillParticipantMapStruct {

    /**
     * Convert CreateBillParticipantRequest to BillParticipant entity.
     * 
     * @param request The create request DTO
     * @return BillParticipant entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "paid", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "user", ignore = true)
    BillParticipant toEntity(CreateBillParticipantRequest request);

    /**
     * Convert BillParticipant entity to BillParticipantDto.
     * 
     * @param billParticipant The bill participant entity
     * @return BillParticipantDto
     */
    @Mapping(target = "billId", source = "id.billId")
    @Mapping(target = "userId", source = "id.userId")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "billTitle", source = "bill.title")
    @Mapping(target = "isPaid", expression = "java(billParticipant.isPaid())")
    BillParticipantDto toDto(BillParticipant billParticipant);

    /**
     * Convert BillParticipant entity to BillParticipantSummaryDto.
     * 
     * @param billParticipant The bill participant entity
     * @return BillParticipantSummaryDto
     */
    @Mapping(target = "billId", source = "id.billId")
    @Mapping(target = "userId", source = "id.userId")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "billTitle", source = "bill.title")
    @Mapping(target = "isPaid", expression = "java(billParticipant.isPaid())")
    BillParticipantSummaryDto toSummaryDto(BillParticipant billParticipant);

    /**
     * Update BillParticipant entity from UpdateBillParticipantRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request         The update request DTO
     * @param billParticipant The bill participant entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(UpdateBillParticipantRequest request, @MappingTarget BillParticipant billParticipant);

    /**
     * Convert list of BillParticipant entities to list of BillParticipantDto.
     * 
     * @param billParticipants List of bill participant entities
     * @return List of BillParticipantDto
     */
    List<BillParticipantDto> toDtoList(List<BillParticipant> billParticipants);

    /**
     * Convert list of BillParticipant entities to list of
     * BillParticipantSummaryDto.
     * 
     * @param billParticipants List of bill participant entities
     * @return List of BillParticipantSummaryDto
     */
    List<BillParticipantSummaryDto> toSummaryDtoList(List<BillParticipant> billParticipants);
}
