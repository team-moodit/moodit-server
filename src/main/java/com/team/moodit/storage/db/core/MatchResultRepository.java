package com.team.moodit.storage.db.core;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResultEntity, Long> {
    Optional<MatchResultEntity> findByUserIdAndMatchId(Long userId, Long matchId);

    List<MatchResultEntity> findByMatchIdIn(List<Long> matchIds);
}
