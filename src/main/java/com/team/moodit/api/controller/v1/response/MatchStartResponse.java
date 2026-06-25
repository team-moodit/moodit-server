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
        // 동적 계산: 현재 진행 라운드가 총 라운드 수를 초과했는지 체크
        boolean completed = domain.getCurrentRound() > domain.getTotalRounds();

        return new MatchStartResponse(
                domain.getTournamentTitle(),
                domain.getTotalRounds(),
                domain.getCurrentRound(),
                domain.getRoundName(),
                completed,
                new NextMatchUpResponse(
                        new CandidateResponse(domain.getCandidateAId(), domain.getCandidateAUrl()),
                        new CandidateResponse(domain.getCandidateBId(), domain.getCandidateBUrl())
                ),
                domain.getReasons() == null ? List.of() : domain.getReasons().stream()
                        .map(r -> new ReasonResponse(r.getId(), r.getContent()))
                        .toList()
        );
    }


    private record NextMatchUpResponse(CandidateResponse candidateA, CandidateResponse candidateB) {}
    private record CandidateResponse(Long id, String photoUri) {}
    private record ReasonResponse(Long id, String content) {}
}