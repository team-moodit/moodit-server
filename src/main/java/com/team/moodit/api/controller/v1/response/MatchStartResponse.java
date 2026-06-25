package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.Match;
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
    public static MatchStartResponse of(Match domain) {
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
                        .map(r -> new ReasonResponse(r.id(), r.content()))
                        .toList()
        );
    }
}

// 📌 컴파일 에러 방지를 위해 하단에 하위 레코드들도 한 파일에 다 쑤셔 넣어둡니다.
record NextMatchUpResponse(CandidateResponse candidateA, CandidateResponse candidateB) {}
record CandidateResponse(Long id, String photoUri) {}
record ReasonResponse(Long id, String content) {}