package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.domain.match.InProgressMatches;

import java.time.LocalDateTime;
import java.util.List;

public record InProgressMatchesResponse(
        List<Content> content,
        long totalCount,
        boolean hasNext
) {

    public static InProgressMatchesResponse of(InProgressMatches result) {
        return new InProgressMatchesResponse(
                result.getContent().stream()
                        .map(match -> new Content(
                                match.getMatchId(),
                                match.getMatchResultId(),
                                match.getMatchState(),      // 추가
                                match.getTitle(),
                                match.getCurrentRound(),
                                match.getTotalRound(),
                                match.getLastPlayedAt()
                        ))
                        .toList(),
                result.getTotalCount(),
                result.isHasNext()
        );
    }

    public record Content(
            Long matchId,
            Long matchResultId,
            MatchState matchState,          // 추가
            String title,
            Integer currentRound,
            Integer totalRound,
            LocalDateTime lastPlayedAt
    ) {
    }
}