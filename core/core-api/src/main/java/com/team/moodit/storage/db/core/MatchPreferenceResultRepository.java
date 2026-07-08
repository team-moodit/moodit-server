package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface MatchPreferenceResultRepository extends JpaRepository<MatchPreferenceResultEntity, Long> {
    List<MatchPreferenceResultEntity> findByMatchResultId(Long matchResultId);
    List<MatchPreferenceResultEntity> findByMatchResultIdIn(List<Long> matchResultIds);

    @Query(
        """
        SELECT preference.preferenceType as preferenceType, SUM(preference.selectedCount) as count
        FROM MatchPreferenceResultEntity preference
            JOIN MatchResultEntity matchResult ON preference.matchResultId = matchResult.id
        WHERE matchResult.userId = :userId
        GROUP BY preference.preferenceType
        """
    )
    List<PreferenceSelectionCountProjection> countPreferenceSelectionByUserId(Long userId);
}
