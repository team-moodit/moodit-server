package com.team.moodit.api.controller.v1.response;

import java.util.List;

public record MatchUpFlowResponse(
        String tournamentTitle,
        String roundTitle,             // 예) "8강전"
        int currentMatchIndex,         // 예) 1 (8강전 첫 번째 경기)
        int totalMatchUpInRound,       // 예) 4 (8강전 전체 경기 수)
        boolean isTournamentCompleted,
        MatchStartResponse.NextMatchUpResponse nextMatchUp,
        List<MatchStartResponse.ReasonResponse> reasons
) {
}
