package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchUpRepository extends JpaRepository<MatchUpEntity,Long> {
    List<MatchUpEntity> findByMatchId(Long matchId);
}
