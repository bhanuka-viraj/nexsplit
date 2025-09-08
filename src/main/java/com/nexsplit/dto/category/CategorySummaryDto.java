package com.nexsplit.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDto {

    private String id;
    private String name;
    private String createdBy;
    private String nexId;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // Lightweight fields only
    private String creatorName;
    private String creatorUsername;
    private String nexName;
}
