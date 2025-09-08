package com.nexsplit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "created_by", nullable = false, columnDefinition = "CHAR(36)")
    private String createdBy;

    @Column(name = "nex_id", columnDefinition = "CHAR(36)")
    private String nexId;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    // Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nex_id", insertable = false, updatable = false)
    private Nex nex;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Expense> expenses;

    @PrePersist
    protected void onCreate() {
        if (isDefault == null) {
            isDefault = false;
        }
    }
}
