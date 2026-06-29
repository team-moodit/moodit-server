package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.MatchUpStart;
import java.util.List;

public record MatchStartResponse(
        String tournamentTitle,
        int totalRounds,
        int currentRound,
        String roundName,
        boolean isTournamentCompleted,
        NextMatchUpResponse nextMatchUp,
        List<ReasonResponse> reasons
) {
    public static MatchStartResponse of(MatchUpStart domain) {
        boolean completed = domain.getCurrentRound() > domain.getTotalRounds();

        return new MatchStartResponse(
                domain.getTournamentTitle(),
                domain.getTotalRounds(),
                domain.getCurrentRound(),
                domain.getRoundName(),
                completed,
                new NextMatchUpResponse(
                        domain.getMatchUpId(), // 🎯 여기에 도메인에서 꺼낸 대진표 ID 주입!
                        new CandidateResponse(domain.getCandidateAId(), domain.getCandidateAUrl()),
                        new CandidateResponse(domain.getCandidateBId(), domain.getCandidateBUrl())
                ),
                domain.getReasons() == null ? List.of() : domain.getReasons().stream()
                        .map(r -> new ReasonResponse(
                                r.getVoteId(),
                                r.getContent()
                        ))
                        .toList()
        );
    }

    //  지훈님 요구사항에 맞춰 matchUpId 필드 추가!
    public record NextMatchUpResponse(
            Long matchUpId,
            CandidateResponse candidateA,
            CandidateResponse candidateB
    ) {}

    public record CandidateResponse(Long id, String photoUri) {}
    public record ReasonResponse(Long id, String content) {}
}