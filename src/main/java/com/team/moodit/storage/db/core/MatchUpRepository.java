package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface MatchUpRepository extends JpaRepository<MatchUpEntity,Long> {
    @Query("SELECT m FROM MatchUpEntity m WHERE m.matchId = :matchId ORDER BY m.roundNumber ASC, m.id ASC")
    List<MatchUpEntity> findByMatchId(@Param("matchId") Long matchId);
}
