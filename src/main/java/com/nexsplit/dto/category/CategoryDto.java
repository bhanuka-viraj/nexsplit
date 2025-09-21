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
public class CategoryDto {

    private String id;
    private String name;
    private String createdBy;
    private String nexId;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // Additional fields for response
    private String creatorName;
    private String creatorUsername;
    private String nexName;
    private Long expenseCount;
    private java.math.BigDecimal totalExpenseAmount;
}
