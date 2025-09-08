package com.nexsplit.dto.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @Size(min = 1, max = 255, message = "Category name must be between 1 and 255 characters")
    private String name;
}
