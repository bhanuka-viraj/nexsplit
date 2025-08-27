package com.nexsplit.repository;

import com.nexsplit.model.Nex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NexRepository extends SoftDeleteRepository<Nex, String> {

    @Query("SELECT n FROM Nex n JOIN n.members nm WHERE nm.user.id = :userId AND nm.status = 'ACTIVE' AND n.isDeleted = false")
    List<Nex> findByMembersUserIdAndStatus(@Param("userId") String userId);

    @Query("SELECT n FROM Nex n JOIN n.members nm WHERE nm.user.id = :userId AND n.id = :nexId AND nm.status = 'ACTIVE' AND n.isDeleted = false")
    Optional<Nex> findByIdAndMembersUserId(@Param("nexId") String nexId, @Param("userId") String userId);

    @Query("SELECT n FROM Nex n WHERE n.createdBy = :userId AND n.isDeleted = false")
    List<Nex> findByCreatedBy(@Param("userId") String userId);

    @Query("SELECT n FROM Nex n JOIN n.members nm WHERE nm.user.id = :userId AND nm.status = 'ACTIVE' AND n.isDeleted = false")
    Page<Nex> findByMembersUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Nex n JOIN n.members nm WHERE nm.user.id = :userId AND nm.status = 'ACTIVE' AND n.isDeleted = false")
    long countActiveNexesByUserId(@Param("userId") String userId);

    @Query("SELECT n FROM Nex n WHERE n.nexType = :nexType AND n.isDeleted = false")
    List<Nex> findByNexType(@Param("nexType") Nex.NexType nexType);
}
