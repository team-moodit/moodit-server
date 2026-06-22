package com.team.moodit.storage.db.core;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchPreferenceResultRepository extends JpaRepository<MatchPreferenceResultEntity, Long> {
    List<MatchPreferenceResultEntity> findByMatchResultId(Long matchResultId);
}
