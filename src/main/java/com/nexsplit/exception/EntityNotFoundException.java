package com.nexsplit.exception;

import com.nexsplit.dto.ErrorCode;
import com.nexsplit.model.*;
import lombok.Getter;

/**
 * Exception thrown when a requested entity is not found in the database.
 * This exception is used for 404 Not Found responses.
 */
@Getter
public class EntityNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Class<?> entityType;
    private final String entityId;

    /**
     * Creates a new EntityNotFoundException with a generic message.
     * 
     * @param entityType The type of entity that was not found
     * @param entityId   The ID of the entity that was not found
     * @param errorCode  The specific error code for this entity type
     */
    public EntityNotFoundException(Class<?> entityType, String entityId, ErrorCode errorCode) {
        super(String.format("%s with ID '%s' not found", entityType.getSimpleName(), entityId));
        this.entityType = entityType;
        this.entityId = entityId;
        this.errorCode = errorCode;
    }

    /**
     * Creates a new EntityNotFoundException with a custom message.
     * 
     * @param message    Custom error message
     * @param entityType The type of entity that was not found
     * @param entityId   The ID of the entity that was not found
     * @param errorCode  The specific error code for this entity type
     */
    public EntityNotFoundException(String message, Class<?> entityType, String entityId, ErrorCode errorCode) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
        this.errorCode = errorCode;
    }

    /**
     * Creates a new EntityNotFoundException with a custom message and cause.
     * 
     * @param message    Custom error message
     * @param entityType The type of entity that was not found
     * @param entityId   The ID of the entity that was not found
     * @param errorCode  The specific error code for this entity type
     * @param cause      The cause of this exception
     */
    public EntityNotFoundException(String message, Class<?> entityType, String entityId, ErrorCode errorCode,
            Throwable cause) {
        super(message, cause);
        this.entityType = entityType;
        this.entityId = entityId;
        this.errorCode = errorCode;
    }

    /**
     * Static factory method for creating User not found exceptions.
     */
    public static EntityNotFoundException userNotFound(String userId) {
        return new EntityNotFoundException(
                "User not found",
                User.class,
                userId,
                ErrorCode.USER_NOT_FOUND);
    }

    /**
     * Static factory method for creating Nex not found exceptions.
     */
    public static EntityNotFoundException nexNotFound(String nexId) {
        return new EntityNotFoundException(
                "Expense group not found",
                Nex.class,
                nexId,
                ErrorCode.NEX_NOT_FOUND);
    }

    /**
     * Static factory method for creating Expense not found exceptions.
     */
    public static EntityNotFoundException expenseNotFound(String expenseId) {
        return new EntityNotFoundException(
                "Expense not found",
                Expense.class,
                expenseId,
                ErrorCode.EXPENSE_NOT_FOUND);
    }

    /**
     * Static factory method for creating Category not found exceptions.
     */
    public static EntityNotFoundException categoryNotFound(String categoryId) {
        return new EntityNotFoundException(
                "Category not found",
                Category.class,
                categoryId,
                ErrorCode.CATEGORY_NOT_FOUND);
    }

    /**
     * Static factory method for creating Debt not found exceptions.
     */
    public static EntityNotFoundException debtNotFound(String debtId) {
        return new EntityNotFoundException(
                "Debt not found",
                Debt.class,
                debtId,
                ErrorCode.DEBT_NOT_FOUND);
    }

    /**
     * Static factory method for creating Attachment not found exceptions.
     */
    public static EntityNotFoundException attachmentNotFound(String attachmentId) {
        return new EntityNotFoundException(
                "Attachment not found",
                Attachment.class,
                attachmentId,
                ErrorCode.ATTACHMENT_NOT_FOUND);
    }

    /**
     * Static factory method for creating Bill not found exceptions.
     */
    public static EntityNotFoundException billNotFound(String billId) {
        return new EntityNotFoundException(
                "Bill not found",
                Bill.class,
                billId,
                ErrorCode.BILL_NOT_FOUND);
    }

    /**
     * Static factory method for creating BillParticipant not found exceptions.
     */
    public static EntityNotFoundException billParticipantNotFound(String billId, String userId) {
        return new EntityNotFoundException(
                String.format("Bill participant not found for bill %s and user %s", billId, userId),
                BillParticipant.class,
                String.format("%s-%s", billId, userId),
                ErrorCode.BILL_PARTICIPANT_NOT_FOUND);
    }
}
