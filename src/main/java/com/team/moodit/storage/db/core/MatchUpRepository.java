package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.domain.enums.MatchUpState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface MatchUpRepository extends JpaRepository<MatchUpEntity,Long> {
    @Query("SELECT m FROM MatchUpEntity m WHERE m.matchId = :matchId ORDER BY m.roundNumber ASC, m.id ASC")
    List<MatchUpEntity> findByMatchId(@Param("matchId") Long matchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchUpEntity m WHERE m.matchId = :matchId ORDER BY m.roundNumber ASC, m.id ASC")
    List<MatchUpEntity> findByMatchIdWithLock(@Param("matchId") Long matchId);

    // 📌 아래 파라미터 타입을 MatchUpState로 변경!
    Optional<MatchUpEntity> findFirstByMatchIdAndState(Long matchId, MatchUpState state);

    // 📌 아래 파라미터 타입을 MatchUpState로 변경!
    int countByMatchIdAndState(Long matchId, MatchUpState state);
}
