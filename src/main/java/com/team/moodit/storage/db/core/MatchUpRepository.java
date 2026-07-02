package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchUpState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchUpRepository extends JpaRepository<MatchUpEntity, Long> {

    @Query("SELECT m FROM MatchUpEntity m WHERE m.matchId = :matchId ORDER BY m.roundNumber ASC, m.id ASC")
    List<MatchUpEntity> findByMatchId(@Param("matchId") Long matchId);

    Optional<MatchUpEntity> findFirstByMatchIdAndState(Long matchId, MatchUpState state);

    int countByMatchIdAndState(Long matchId, MatchUpState state);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchUpEntity m WHERE m.id = :id")
    Optional<MatchUpEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT mc FROM MatchVoteCandidateEntity mc WHERE mc.matchId = :matchId")
    List<MatchVoteCandidateEntity> findVotedLabelsByMatchId(@Param("matchId") Long matchId);

    void deleteByMatchId(Long matchId);

    List<MatchUpEntity> findByMatchIdIn(List<Long> matchIds);

    List<MatchUpEntity> findByMatchIdOrderByRoundNumberAscIdAsc(Long matchId);
}