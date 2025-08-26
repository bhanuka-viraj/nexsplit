package com.nexsplit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaInfo {
    private String errorCode;
    private String errorType;
    private String documentationUrl;
    private String requestId;
    private Long responseTime;
    private String version;
}
