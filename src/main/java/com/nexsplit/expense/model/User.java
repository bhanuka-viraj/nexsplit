package com.nexsplit.expense.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private String id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column
    private String password;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum Role { USER, ADMIN }
}