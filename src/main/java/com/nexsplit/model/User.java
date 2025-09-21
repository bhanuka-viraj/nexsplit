package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String password;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "last_validation_code")
    @lombok.Builder.Default
    private Integer lastValidationCode = 0;

    @Column(name = "is_email_validate")
    @lombok.Builder.Default
    private Boolean isEmailValidate = false;

    @Column(name = "is_google_auth")
    @lombok.Builder.Default
    private Boolean isGoogleAuth = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE, INACTIVE
    }

    /**
     * Ensure default values are set before persisting
     */
    @PrePersist
    protected void onUserPrePersist() {
        // Ensure BaseEntity default values are set
        ensureDefaultValues();

        // Set User-specific default values
        if (lastValidationCode == null) {
            lastValidationCode = 0;
        }
        if (isEmailValidate == null) {
            isEmailValidate = false;
        }
        if (isGoogleAuth == null) {
            isGoogleAuth = false;
        }
        if (status == null) {
            status = Status.ACTIVE;
        }
    }

    // Helper method to get full name
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    // Helper method to set full name
    public void setFullName(String fullName) {
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            this.firstName = parts[0];
            this.lastName = parts[1];
        } else {
            this.firstName = fullName;
            this.lastName = null;
        }
    }

    // Helper method to check if user is active
    public boolean isActive() {
        return Status.ACTIVE.equals(this.status) && !isDeleted();
    }

    // Helper method to soft delete user
    public void softDelete(String deletedBy) {
        this.status = Status.INACTIVE;
        super.softDelete(deletedBy);
    }

    // Relationships (lazy loaded to avoid circular dependencies)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;
}