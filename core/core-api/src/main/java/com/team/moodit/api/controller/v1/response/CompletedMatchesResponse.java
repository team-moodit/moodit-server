package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.CompletedMatches;

import java.time.LocalDate;
import java.util.List;

public record CompletedMatchesResponse(
        List<Content> content,
        long totalCount,
        boolean hasNext
) {
    public static CompletedMatchesResponse of(CompletedMatches result) {
        return new CompletedMatchesResponse(
                result.getContent().stream()
                        .map(match -> new Content(
                                match.getUserMissionId(),
                                match.getMatchId(),
                                match.getTitle(),
                                match.getWinnerImageId(),
                                match.getWinnerImageUri(),
                                match.getCompletedAt()
                        ))
                        .toList(),
                result.getTotalCount(),
                result.isHasNext()
        );
    }

    public record Content(
            Long userMissionId,
            Long matchId,
            String title,
            Long winnerImageId,
            String winnerImageUri,
            LocalDate completedAt
    ) {
    }
}