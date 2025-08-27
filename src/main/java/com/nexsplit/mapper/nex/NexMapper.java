package com.nexsplit.mapper.nex;

import com.nexsplit.dto.nex.NexDto;
import com.nexsplit.model.Nex;
import org.springframework.stereotype.Component;

@Component
public class NexMapper {

    public NexDto toDto(Nex nex) {
        if (nex == null) {
            return null;
        }

        String creatorName = null;
        String creatorUsername = null;
        if (nex.getCreator() != null) {
            creatorName = nex.getCreator().getFirstName() + " " + nex.getCreator().getLastName();
            creatorUsername = nex.getCreator().getUsername();
        }

        int memberCount = nex.getMembers() != null ? nex.getMembers().size() : 0;
        int expenseCount = nex.getExpenses() != null ? nex.getExpenses().size() : 0;
        int categoryCount = nex.getCategories() != null ? nex.getCategories().size() : 0;

        return NexDto.builder()
                .id(nex.getId())
                .name(nex.getName())
                .description(nex.getDescription())
                .imageUrl(nex.getImageUrl())
                .createdBy(nex.getCreatedBy())
                .settlementType(nex.getSettlementType())
                .isArchived(nex.getIsArchived())
                .nexType(nex.getNexType())
                .createdAt(nex.getCreatedAt())
                .modifiedAt(nex.getModifiedAt())
                .creatorName(creatorName)
                .creatorUsername(creatorUsername)
                .memberCount(memberCount)
                .expenseCount(expenseCount)
                .categoryCount(categoryCount)
                .build();
    }

    public Nex toEntity(com.nexsplit.dto.nex.CreateNexRequest request) {
        if (request == null) {
            return null;
        }

        return Nex.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .settlementType(request.getSettlementType())
                .nexType(request.getNexType())
                .build();
    }

    public void updateEntityFromRequest(com.nexsplit.dto.nex.UpdateNexRequest request, Nex nex) {
        if (request == null || nex == null) {
            return;
        }

        if (request.getName() != null) {
            nex.setName(request.getName());
        }
        if (request.getDescription() != null) {
            nex.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            nex.setImageUrl(request.getImageUrl());
        }
        if (request.getSettlementType() != null) {
            nex.setSettlementType(request.getSettlementType());
        }
        if (request.getNexType() != null) {
            nex.setNexType(request.getNexType());
        }
        if (request.getIsArchived() != null) {
            nex.setIsArchived(request.getIsArchived());
        }
    }
}
