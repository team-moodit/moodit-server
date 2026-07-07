package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchImageRepository extends JpaRepository<MatchImageEntity, Long> {
    List<MatchImageEntity> findByMatchId(Long matchId);
}
