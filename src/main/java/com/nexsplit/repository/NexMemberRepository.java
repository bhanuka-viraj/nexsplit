package com.nexsplit.repository;

import com.nexsplit.model.NexMember;
import com.nexsplit.model.NexMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NexMemberRepository extends JpaRepository<NexMember, NexMemberId> {

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.status = 'ACTIVE'")
    List<NexMember> findActiveMembersByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId")
    List<NexMember> findAllMembersByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.userId = :userId AND nm.status = 'ACTIVE'")
    List<NexMember> findActiveMembershipsByUserId(@Param("userId") String userId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.id.userId = :userId")
    Optional<NexMember> findByNexIdAndUserId(@Param("nexId") String nexId, @Param("userId") String userId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.role = 'ADMIN'")
    List<NexMember> findAdminsByNexId(@Param("nexId") String nexId);

    @Query("SELECT COUNT(nm) FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.status = 'ACTIVE'")
    long countActiveMembersByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.nexId = :nexId AND nm.status = 'PENDING'")
    List<NexMember> findPendingMembersByNexId(@Param("nexId") String nexId);

    @Query("SELECT nm FROM NexMember nm WHERE nm.id.userId = :userId AND nm.status = 'PENDING'")
    List<NexMember> findPendingInvitationsByUserId(@Param("userId") String userId);

    @Query("DELETE FROM NexMember nm WHERE nm.id.nexId = :nexId")
    void deleteByNexId(@Param("nexId") String nexId);
}
