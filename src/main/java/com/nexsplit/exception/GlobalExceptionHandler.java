package com.nexsplit.exception;

import com.nexsplit.config.filter.CorrelationIdFilter;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.ErrorCode;
import com.nexsplit.util.StructuredLoggingUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
                StructuredLoggingUtil.logErrorEvent(
                                "VALIDATION_ERROR",
                                ex.getMessage(),
                                ex.getStackTrace()[0].toString(),
                                Map.of("exception", ex.getClass().getSimpleName()));

                ApiResponse<Void> response = ApiResponse.<Void>error(ex.getMessage(),
                                ErrorCode.VALIDATION_INVALID_FORMAT);
                response.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(AuthenticationServiceException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationServiceException ex) {
                StructuredLoggingUtil.logSecurityEvent(
                                "AUTHENTICATION_FAILED",
                                "system",
                                "unknown",
                                "unknown",
                                "HIGH",
                                Map.of("exception", ex.getClass().getSimpleName(), "message", ex.getMessage()));

                ApiResponse<Void> response = ApiResponse.<Void>error("Authentication failed: " + ex.getMessage(),
                                ErrorCode.AUTH_INVALID_CREDENTIALS);
                response.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
                StructuredLoggingUtil.logErrorEvent(
                                "USER_NOT_FOUND",
                                ex.getMessage(),
                                ex.getStackTrace()[0].toString(),
                                Map.of("exception", ex.getClass().getSimpleName()));

                ApiResponse<Void> response = ApiResponse.<Void>error(ex.getMessage(), ErrorCode.USER_NOT_FOUND);
                response.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
                StructuredLoggingUtil.logErrorEvent(
                                "ENTITY_NOT_FOUND",
                                ex.getMessage(),
                                ex.getStackTrace()[0].toString(),
                                Map.of(
                                                "exception", ex.getClass().getSimpleName(),
                                                "entityType", ex.getEntityType().getSimpleName(),
                                                "entityId", ex.getEntityId()));

                ApiResponse<Void> response = ApiResponse.<Void>error(ex.getMessage(), ex.getErrorCode());
                response.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        @ExceptionHandler(UserUnauthorizedException.class)
        public ResponseEntity<ApiResponse<Void>> handleUserUnauthorizedException(UserUnauthorizedException ex) {
                StructuredLoggingUtil.logSecurityEvent(
                                "USER_UNAUTHORIZED",
                                "system",
                                "unknown",
                                "unknown",
                                "MEDIUM",
                                Map.of("exception", ex.getClass().getSimpleName(), "message", ex.getMessage()));

                ApiResponse<Void> response = ApiResponse.<Void>error(ex.getMessage(),
                                ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS);
                response.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException ex) {
                List<String> fieldErrors = extractFieldErrors(ex.getBindingResult());

                StructuredLoggingUtil.logErrorEvent(
                                "VALIDATION_ERROR",
                                "Validation failed for " + ex.getBindingResult().getErrorCount() + " fields",
                                ex.getStackTrace()[0].toString(),
                                Map.of(
                                                "fieldErrors", fieldErrors,
                                                "objectName", ex.getBindingResult().getObjectName()));

                ApiResponse<Void> response = ApiResponse.<Void>error("Validation failed", fieldErrors);
                response.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
                StructuredLoggingUtil.logBusinessEvent(
                                "BUSINESS_RULE_VIOLATION",
                                "system",
                                "BusinessException",
                                ex.getMessage(),
                                Map.of(
                                                "errorCode", ex.getErrorCode().getCode(),
                                                "errorType", ex.getErrorCode().getType()));

                ApiResponse<Void> response = ApiResponse.<Void>error(ex.getMessage(), ex.getErrorCode());
                response.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
                StructuredLoggingUtil.logErrorEvent(
                                "INTERNAL_SERVER_ERROR",
                                ex.getMessage(),
                                ex.getStackTrace()[0].toString(),
                                Map.of(
                                                "exception", ex.getClass().getSimpleName(),
                                                "stackTrace", ex.getStackTrace()[0].toString()));

                ApiResponse<Void> response = ApiResponse.<Void>error("An internal server error occurred",
                                ErrorCode.INTERNAL_SERVER_ERROR);
                response.setCorrelationId(CorrelationIdFilter.getCurrentCorrelationId());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        private List<String> extractFieldErrors(BindingResult bindingResult) {
                return bindingResult.getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.toList());
        }
}