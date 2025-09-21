package com.nexsplit.mapper.attachment;

import com.nexsplit.dto.attachment.AttachmentDto;
import com.nexsplit.dto.attachment.AttachmentSummaryDto;
import com.nexsplit.dto.attachment.CreateAttachmentRequest;
import com.nexsplit.dto.attachment.UpdateAttachmentRequest;
import com.nexsplit.model.Attachment;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Attachment entities and DTOs.
 * 
 * This mapper provides type-safe, compile-time generated mapping code for
 * converting between Attachment entities and their corresponding DTOs.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttachmentMapStruct {

    /**
     * Convert CreateAttachmentRequest to Attachment entity.
     * 
     * @param request The create request DTO
     * @return Attachment entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "expense", ignore = true)
    @Mapping(target = "uploader", ignore = true)
    Attachment toEntity(CreateAttachmentRequest request);

    /**
     * Convert Attachment entity to AttachmentDto.
     * 
     * @param attachment The attachment entity
     * @return AttachmentDto
     */
    @Mapping(target = "expenseTitle", source = "expense.title")
    @Mapping(target = "uploaderName", source = "uploader.fullName")
    @Mapping(target = "fileExtension", expression = "java(attachment.getFileExtension())")
    @Mapping(target = "isImage", expression = "java(attachment.isImage())")
    @Mapping(target = "isDocument", expression = "java(attachment.isDocument())")
    AttachmentDto toDto(Attachment attachment);

    /**
     * Convert Attachment entity to AttachmentSummaryDto.
     * 
     * @param attachment The attachment entity
     * @return AttachmentSummaryDto
     */
    @Mapping(target = "expenseTitle", source = "expense.title")
    @Mapping(target = "uploaderName", source = "uploader.fullName")
    @Mapping(target = "fileExtension", expression = "java(attachment.getFileExtension())")
    @Mapping(target = "isImage", expression = "java(attachment.isImage())")
    @Mapping(target = "isDocument", expression = "java(attachment.isDocument())")
    AttachmentSummaryDto toSummaryDto(Attachment attachment);

    /**
     * Update Attachment entity from UpdateAttachmentRequest.
     * Only updates non-null fields from the request.
     * 
     * @param request    The update request DTO
     * @param attachment The attachment entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "expenseId", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "expense", ignore = true)
    @Mapping(target = "uploader", ignore = true)
    void updateEntityFromRequest(UpdateAttachmentRequest request, @MappingTarget Attachment attachment);

    /**
     * Convert list of Attachment entities to list of AttachmentDto.
     * 
     * @param attachments List of attachment entities
     * @return List of AttachmentDto
     */
    List<AttachmentDto> toDtoList(List<Attachment> attachments);

    /**
     * Convert list of Attachment entities to list of AttachmentSummaryDto.
     * 
     * @param attachments List of attachment entities
     * @return List of AttachmentSummaryDto
     */
    List<AttachmentSummaryDto> toSummaryDtoList(List<Attachment> attachments);
}
