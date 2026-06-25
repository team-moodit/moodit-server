package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.MatchUpStart; // 📌 우리가 만든 도메인 객체로 정확히 임포트
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
    public static MatchStartResponse of(MatchUpStart domain) { //  타입을 MatchUpStart로 일치
        return new MatchStartResponse(
                domain.getTournamentTitle(),
                domain.getTotalRounds(),
                domain.getCurrentRound(),
                domain.getRoundName(),
                false,
                new NextMatchUpResponse(
                        new CandidateResponse(domain.getCandidateAId(), domain.getCandidateAUrl()),
                        new CandidateResponse(domain.getCandidateBId(), domain.getCandidateBUrl())
                ),
                domain.getReasons().stream()
                        .map(r -> new ReasonResponse(r.getId(), r.getContent()))
                        .toList()
        );
    }
}

record NextMatchUpResponse(CandidateResponse candidateA, CandidateResponse candidateB) {}
record CandidateResponse(Long id, String photoUri) {}
record ReasonResponse(Long id, String content) {}