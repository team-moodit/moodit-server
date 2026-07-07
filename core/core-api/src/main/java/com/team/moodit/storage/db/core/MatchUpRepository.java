package com.team.moodit.storage.db.core;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchUpRepository extends JpaRepository<MatchUpEntity, Long> {
    List<MatchUpEntity> findByMatchIdOrderByRoundNumberAscIdAsc(Long matchId);
    List<MatchUpEntity> findByMatchIdIn(List<Long> matchIds);
    void deleteByMatchId(Long matchId);

    @Query("SELECT m FROM MatchUpEntity m WHERE m.matchId = :matchId ORDER BY m.roundNumber ASC, m.id ASC")
    List<MatchUpEntity> findByMatchId(@Param("matchId") Long matchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchUpEntity m WHERE m.id = :id")
    Optional<MatchUpEntity> findByIdForUpdate(@Param("id") Long id);
}
