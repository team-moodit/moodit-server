package com.team.moodit.storage.db.core;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResultEntity, Long> {
    Optional<MatchResultEntity> findByUserIdAndMatchId(Long userId, Long matchId);
}
