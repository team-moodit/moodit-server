package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchPreferenceResultRepository extends JpaRepository<MatchPreferenceResultEntity, Long> {
    List<MatchPreferenceResultEntity> findByMatchResultId(Long matchResultId);

    List<MatchPreferenceResultEntity> findByMatchResultIdIn(List<Long> matchResultIds);

    void deleteByMatchResultId(Long id);
}
