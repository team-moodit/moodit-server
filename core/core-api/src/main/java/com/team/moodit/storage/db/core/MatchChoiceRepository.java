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

    @Query(
            """
            SELECT mvc.preference AS preference, COUNT(mc.id) AS count
            FROM MatchChoiceEntity mc
            JOIN MatchUpEntity mu
                ON mc.matchUpId = mu.id
            JOIN MatchEntity m
                ON mu.matchId = m.id
            JOIN MatchVoteCandidateEntity mvc
                ON mc.reasonId = mvc.id
            WHERE m.userId = :userId
                AND m.state = com.team.moodit.domain.enums.MatchState.DONE
            GROUP BY mvc.preference
            """
    )
    List<PreferenceVoteCountProjection> countVotedPreferenceByUserId(
            @Param("userId") Long userId
    );

    @Query(
            """
            SELECT mvc.preferenceDetail AS preferenceDetail, COUNT(mc.id) AS count
            FROM MatchChoiceEntity mc
            JOIN MatchUpEntity mu
                ON mc.matchUpId = mu.id
            JOIN MatchEntity m
                ON mu.matchId = m.id
            JOIN MatchVoteCandidateEntity mvc
                ON mc.reasonId = mvc.id
            WHERE m.userId = :userId
                AND m.state = com.team.moodit.domain.enums.MatchState.DONE
                AND mvc.preference = :preference
                AND mvc.preferenceDetail IS NOT NULL
                AND mvc.preferenceDetail <> ''
            GROUP BY mvc.preferenceDetail
            """
    )
    List<PreferenceDetailVoteCountProjection> countVotedPreferenceDetailByUserIdAndPreference(
            @Param("userId") Long userId,
            @Param("preference") String preference
    );
}
