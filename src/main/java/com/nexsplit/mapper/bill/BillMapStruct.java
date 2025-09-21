package com.nexsplit.mapper.bill;

import com.nexsplit.dto.bill.BillDto;
import com.nexsplit.dto.bill.BillSummaryDto;
import com.nexsplit.dto.bill.CreateBillRequest;
import com.nexsplit.dto.bill.UpdateBillRequest;
import com.nexsplit.model.Bill;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Bill entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between Bill entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BillMapStruct {

    /**
     * Convert CreateBillRequest to Bill entity.
     * 
     * @param request The create request DTO
     * @return Bill entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "nex", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "participants", ignore = true)
    Bill toEntity(CreateBillRequest request);

    /**
     * Convert Bill entity to BillDto.
     * 
     * @param bill The bill entity
     * @return BillDto
     */
    @Mapping(target = "nexName", source = "nex.name")
    @Mapping(target = "creatorName", source = "creator.fullName")
    @Mapping(target = "participantCount", expression = "java(bill.getParticipants() != null ? bill.getParticipants().size() : 0)")
    @Mapping(target = "paidParticipantCount", expression = "java(calculatePaidParticipantCount(bill))")
    @Mapping(target = "totalPaidAmount", expression = "java(calculateTotalPaidAmount(bill))")
    BillDto toDto(Bill bill);

    /**
     * Convert Bill entity to BillSummaryDto.
     * 
     * @param bill The bill entity
     * @return BillSummaryDto
     */
    @Mapping(target = "nexName", source = "nex.name")
    @Mapping(target = "creatorName", source = "creator.fullName")
    BillSummaryDto toSummaryDto(Bill bill);

    /**
     * Update Bill entity from UpdateBillRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request The update request DTO
     * @param bill    The bill entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "nexId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "nex", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "participants", ignore = true)
    void updateEntityFromRequest(UpdateBillRequest request, @MappingTarget Bill bill);

    /**
     * Convert list of Bill entities to list of BillDto.
     * 
     * @param bills List of bill entities
     * @return List of BillDto
     */
    List<BillDto> toDtoList(List<Bill> bills);

    /**
     * Convert list of Bill entities to list of BillSummaryDto.
     * 
     * @param bills List of bill entities
     * @return List of BillSummaryDto
     */
    List<BillSummaryDto> toSummaryDtoList(List<Bill> bills);

    /**
     * Calculate number of paid participants for a bill.
     * 
     * @param bill The bill entity
     * @return Number of paid participants
     */
    default int calculatePaidParticipantCount(Bill bill) {
        if (bill.getParticipants() == null || bill.getParticipants().isEmpty()) {
            return 0;
        }
        return (int) bill.getParticipants().stream()
                .filter(participant -> Boolean.TRUE.equals(participant.getPaid()))
                .count();
    }

    /**
     * Calculate total paid amount for a bill.
     * 
     * @param bill The bill entity
     * @return Total paid amount
     */
    default java.math.BigDecimal calculateTotalPaidAmount(Bill bill) {
        if (bill.getParticipants() == null || bill.getParticipants().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        return bill.getParticipants().stream()
                .filter(participant -> Boolean.TRUE.equals(participant.getPaid()))
                .map(participant -> participant.getShareAmount() != null ? participant.getShareAmount()
                        : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
