package com.nexsplit.repository;

import com.nexsplit.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends SoftDeleteRepository<Category, String> {

        /**
         * Find categories by creator with pagination (user's personal categories)
         */
        @Query("SELECT c FROM Category c WHERE c.createdBy = :userId AND c.nexId IS NULL AND c.isDeleted = false")
        Page<Category> findPersonalCategoriesByUserId(@Param("userId") String userId, Pageable pageable);

        /**
         * Find categories by nex ID (group categories)
         */
        @Query("SELECT c FROM Category c WHERE c.nexId = :nexId AND c.isDeleted = false")
        List<Category> findByNexId(@Param("nexId") String nexId);

        /**
         * Find categories by nex ID with pagination (group categories)
         */
        @Query("SELECT c FROM Category c WHERE c.nexId = :nexId AND c.isDeleted = false")
        Page<Category> findByNexIdPaginated(@Param("nexId") String nexId, Pageable pageable);

        /**
         * Find categories by creator and nex ID
         */
        @Query("SELECT c FROM Category c WHERE c.createdBy = :userId AND c.nexId = :nexId AND c.isDeleted = false")
        List<Category> findByCreatedByAndNexId(@Param("userId") String userId, @Param("nexId") String nexId);

        /**
         * Find category by ID and creator (for authorization)
         */
        @Query("SELECT c FROM Category c WHERE c.id = :categoryId AND c.createdBy = :userId AND c.isDeleted = false")
        Optional<Category> findByIdAndCreatedBy(@Param("categoryId") String categoryId, @Param("userId") String userId);

        /**
         * Find category by ID and nex ID (for nex-specific authorization)
         */
        @Query("SELECT c FROM Category c WHERE c.id = :categoryId AND c.nexId = :nexId AND c.isDeleted = false")
        Optional<Category> findByIdAndNexId(@Param("categoryId") String categoryId, @Param("nexId") String nexId);

        /**
         * Check if category name exists for user (personal categories)
         */
        @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.createdBy = :userId AND c.nexId IS NULL AND c.isDeleted = false")
        boolean existsByNameAndCreatedBy(@Param("name") String name, @Param("userId") String userId);

        /**
         * Check if category name exists for nex (group categories)
         */
        @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.nexId = :nexId AND c.isDeleted = false")
        boolean existsByNameAndNexId(@Param("name") String name, @Param("nexId") String nexId);

        /**
         * Count categories by nex ID
         */
        @Query("SELECT COUNT(c) FROM Category c WHERE c.nexId = :nexId AND c.isDeleted = false")
        long countByNexId(@Param("nexId") String nexId);

        /**
         * Count personal categories by user ID
         */
        @Query("SELECT COUNT(c) FROM Category c WHERE c.createdBy = :userId AND c.nexId IS NULL AND c.isDeleted = false")
        long countPersonalCategoriesByUserId(@Param("userId") String userId);

        /**
         * Find personal categories by user ID with pagination
         */
        @Query("SELECT c FROM Category c WHERE c.createdBy = :userId AND c.nexId IS NULL AND c.isDeleted = false")
        Page<Category> findPersonalCategoriesByUserIdPaginated(@Param("userId") String userId, Pageable pageable);

        /**
         * Find default categories with pagination
         */
        @Query("SELECT c FROM Category c WHERE c.isDefault = true AND c.isDeleted = false")
        Page<Category> findDefaultCategoriesPaginated(Pageable pageable);
}
