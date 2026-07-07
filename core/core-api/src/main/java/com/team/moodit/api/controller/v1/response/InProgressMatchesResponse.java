package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.InProgressMatches;

import java.time.LocalDateTime;
import java.util.List;

public record InProgressMatchesResponse(
        List<Content> content,
        long totalCount,
        boolean hasNext
) {

    public static InProgressMatchesResponse of(InProgressMatches inprogressresult) {
        return new InProgressMatchesResponse(
                inprogressresult.getContent().stream()
                        .map(match -> new Content(
                                match.getMatchId(),
                                match.getTitle(),
                                match.getCurrentRound(),
                                match.getTotalRound(),
                                match.getLastPlayedAt()
                        ))
                        .toList(),
                inprogressresult.getTotalCount(),
                inprogressresult.isHasNext()
        );
    }

    public record Content(
            Long matchId,
            String title,
            Integer currentRound,
            Integer totalRound,
            LocalDateTime lastPlayedAt
    ) {
    }
}