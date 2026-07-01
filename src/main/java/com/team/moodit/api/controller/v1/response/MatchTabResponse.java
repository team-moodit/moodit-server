package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.CompletedMatch;
import com.team.moodit.domain.match.InProgressMatch;
import com.team.moodit.domain.match.MatchTab;
import com.team.moodit.support.Page;
import com.team.moodit.support.response.PageResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;


public record MatchTabResponse(
        PageResponse<InProgressMatchResponse> inProgressMatches,
        PageResponse<CompletedMatchResponse> completedMatches
) {

    public static MatchTabResponse of(MatchTab domain) {
        return new MatchTabResponse(
                PageResponse.of(toInProgressPage(domain)),
                PageResponse.of(toCompletedPage(domain))
                       );
    }

    private static Page<InProgressMatchResponse> toInProgressPage(MatchTab domain) {
        return new Page<>(
                domain.getInProgressMatches().content().stream()
                        .map(InProgressMatchResponse::of)
                        .toList(),
                domain.getInProgressMatches().totalCount(),
                domain.getInProgressMatches().hasNext()
        );
    }

    private static Page<CompletedMatchResponse> toCompletedPage(MatchTab domain) {
        return new Page<>(
                domain.getCompletedMatches().content().stream()
                        .map(CompletedMatchResponse::of)
                        .toList(),
                domain.getCompletedMatches().totalCount(),
                domain.getCompletedMatches().hasNext()
        );
    }

    public record InProgressMatchResponse(
            Long matchId,
            String title,
            int currentRound,
            int totalRound,
            LocalDateTime lastPlayedAt
    ) {
        public static InProgressMatchResponse of(InProgressMatch domain) {
            return new InProgressMatchResponse(
                    domain.getMatchId(),
                    domain.getTitle(),
                    domain.getCurrentRound(),
                    domain.getTotalRound(),
                    domain.getLastPlayedAt()
            );
        }
    }

    public record CompletedMatchResponse(
            Long matchId,
            String title,
            Long winnerImageId,
            String winnerImageUri,
            LocalDate completedAt
    ) {
        public static CompletedMatchResponse of(CompletedMatch domain) {
            return new CompletedMatchResponse(
                    domain.getMatchId(),
                    domain.getTitle(),
                    domain.getWinnerImageId(),
                    domain.getWinnerImageUri(),
                    domain.getCompletedAt()
            );
        }
    }
}