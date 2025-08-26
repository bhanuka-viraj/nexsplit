package com.nexsplit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldError {
    private String field;
    private String code;
    private String message;
    private Object rejectedValue;
    private String objectName;
}
