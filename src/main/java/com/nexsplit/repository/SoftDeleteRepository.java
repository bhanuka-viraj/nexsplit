package com.nexsplit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Find by ID excluding soft deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = false")
    Optional<T> findByIdAndNotDeleted(@Param("id") ID id);

    /**
     * Find all excluding soft deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false")
    List<T> findAllNotDeleted();

    /**
     * Check if entity exists and is not deleted
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = false")
    boolean existsByIdAndNotDeleted(@Param("id") ID id);

    /**
     * Soft delete by ID
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = true, e.deletedAt = CURRENT_TIMESTAMP, e.deletedBy = :deletedBy WHERE e.id = :id")
    void softDeleteById(@Param("id") ID id, @Param("deletedBy") String deletedBy);

    /**
     * Restore soft deleted entity
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = false, e.deletedAt = NULL, e.deletedBy = NULL WHERE e.id = :id")
    void restoreById(@Param("id") ID id);

    /**
     * Find all soft deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = true")
    List<T> findAllDeleted();

    /**
     * Hard delete by ID (use with caution)
     */
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.id = :id")
    void hardDeleteById(@Param("id") ID id);
}
