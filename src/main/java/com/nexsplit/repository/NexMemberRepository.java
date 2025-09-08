package com.nexsplit.repository;

import com.nexsplit.model.NexMember;
import com.nexsplit.model.NexMemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NexMemberRepository extends SoftDeleteRepository<NexMember, NexMemberId> {

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.status = 'ACTIVE' AND nm.isDeleted = false")
    List<NexMember> findActiveMembersByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.isDeleted = false")
    List<NexMember> findAllMembersByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.isDeleted = false")
    Page<NexMember> findAllMembersByNexIdPaginated(@Param("nexId") String nexId, Pageable pageable);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.userId = :userId AND nm.status = 'ACTIVE' AND nm.isDeleted = false")
    List<NexMember> findActiveMembershipsByUserId(@Param("userId") String userId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.userId = :userId AND nm.status = 'ACTIVE' AND nm.isDeleted = false")
    Page<NexMember> findActiveMembershipsByUserIdPaginated(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.id.userId = :userId AND nm.isDeleted = false")
    Optional<NexMember> findByNexIdAndUserId(@Param("nexId") String nexId, @Param("userId") String userId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.role = 'ADMIN' AND nm.isDeleted = false")
    List<NexMember> findAdminsByNexId(@Param("nexId") String nexId);

    @Query("SELECT COUNT(nm) FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.status = 'ACTIVE' AND nm.isDeleted = false")
    long countActiveMembersByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm.id.userId FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.status = 'ACTIVE' AND nm.isDeleted = false")
    List<String> findActiveMemberIdsByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.status = 'PENDING' AND nm.isDeleted = false")
    List<NexMember> findPendingMembersByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.userId = :userId AND nm.status = 'PENDING' AND nm.isDeleted = false")
    List<NexMember> findPendingInvitationsByUserId(@Param("userId") String userId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.userId = :userId AND nm.status = 'PENDING' AND nm.isDeleted = false")
    Page<NexMember> findPendingInvitationsByUserIdPaginated(@Param("userId") String userId, Pageable pageable);

    @Modifying
    @Query("UPDATE NexMember nm SET nm.isDeleted = true, nm.deletedAt = CURRENT_TIMESTAMP WHERE nm.id.nexId = :nexId")
    void softDeleteByNexId(@Param("nexId") String nexId);
}
