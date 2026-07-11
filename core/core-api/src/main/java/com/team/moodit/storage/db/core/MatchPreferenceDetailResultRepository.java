package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchPreferenceDetailResultRepository
        extends JpaRepository<MatchPreferenceDetailResultEntity, Long> {

    List<MatchPreferenceDetailResultEntity>
    findByMatchResultIdOrderByPreferenceTypeAscRankAsc(
            Long matchResultId
    );
}