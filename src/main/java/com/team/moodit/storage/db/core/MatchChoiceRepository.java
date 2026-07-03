package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchChoiceRepository extends JpaRepository<MatchChoiceEntity, Long> {
    void deleteByMatchUpIdIn(List<Long> matchUpIds);

    @Query(
        """
        SELECT mvc
        FROM MatchChoiceEntity mc
        JOIN MatchUpEntity mu
            ON mc.matchUpId = mu.id
        JOIN MatchVoteCandidateEntity mvc
            ON mc.reasonId = mvc.id
        WHERE mu.matchId = :matchId
        """
    )
    List<MatchVoteCandidateEntity> findActualVotedCandidatesByMatchId(@Param("matchId") Long matchId);
}
