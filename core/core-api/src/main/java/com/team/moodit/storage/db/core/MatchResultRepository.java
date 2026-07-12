package com.team.moodit.storage.db.core;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchResultRepository extends JpaRepository<MatchResultEntity, Long> {
    List<MatchResultEntity> findByUserIdOrderByCompletedAtDesc(Long userId);
    Optional<MatchResultEntity> findByUserIdAndMatchId(Long userId, Long matchId);
    List<MatchResultEntity> findByMatchIdIn(List<Long> matchIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchResultEntity m WHERE m.userId = :userId AND m.matchId = :matchId")
    Optional<MatchResultEntity> findByUserIdAndMatchIdForUpdate(@Param("userId") Long userId, @Param("matchId") Long matchId);
    Optional<MatchResultEntity> findByMatchId(Long matchId);




}
