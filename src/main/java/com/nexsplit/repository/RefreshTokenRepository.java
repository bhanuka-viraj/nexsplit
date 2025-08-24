package com.nexsplit.repository;

import com.nexsplit.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken entity
 * Provides methods for secure token management
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

        /**
         * Find refresh token by its hash
         */
        Optional<RefreshToken> findByTokenHash(String tokenHash);

        /**
         * Find all valid refresh tokens for a user
         */
        @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.isUsed = false AND rt.isRevoked = false AND rt.expiresAt > :now")
        List<RefreshToken> findValidTokensByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);

        /**
         * Find all tokens in a family
         */
        List<RefreshToken> findByFamilyId(String familyId);

        /**
         * Count active tokens in a family
         */
        @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.familyId = :familyId AND rt.isUsed = false AND rt.isRevoked = false")
        long countActiveTokensInFamily(@Param("familyId") String familyId);

        /**
         * Revoke all tokens in a family (for security incidents)
         */
        @Modifying
        @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.familyId = :familyId")
        void revokeAllTokensInFamily(@Param("familyId") String familyId);

        /**
         * Clean up expired tokens
         */
        @Modifying
        @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
        void deleteExpiredTokens(@Param("now") LocalDateTime now);

        /**
         * Find tokens by user ID and family ID
         */
        List<RefreshToken> findByUserIdAndFamilyId(String userId, String familyId);

        /**
         * Count unique user agents in a family
         * More efficient than Java stream processing for large families
         */
        @Query("SELECT COUNT(DISTINCT rt.userAgent) FROM RefreshToken rt " +
                        "WHERE rt.familyId = :familyId AND rt.isUsed = false AND rt.isRevoked = false")
        long countUniqueSourcesInFamily(@Param("familyId") String familyId);

        /**
         * Check if family has multiple user agents (optimized version)
         * Returns true if more than 1 unique user agent exists
         */
        @Query("SELECT COUNT(DISTINCT rt.userAgent) > 1 FROM RefreshToken rt " +
                        "WHERE rt.familyId = :familyId AND rt.isUsed = false AND rt.isRevoked = false")
        boolean hasMultipleSourcesInFamily(@Param("familyId") String familyId);

        /**
         * Count recent tokens in family (optimized for rapid generation detection)
         * More efficient than Java stream filtering
         */
        @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
                        "WHERE rt.familyId = :familyId AND rt.createdAt > :cutoffTime")
        long countRecentTokensInFamily(@Param("familyId") String familyId,
                        @Param("cutoffTime") LocalDateTime cutoffTime);
}
