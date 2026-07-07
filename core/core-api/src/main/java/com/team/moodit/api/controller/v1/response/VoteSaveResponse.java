package com.team.moodit.api.controller.v1.response;

public record VoteSaveResponse(
        Long nextMatchId,             // 다음 투표를 진행할 대진 식별자 (없을 경우 null)
        int currentRound,             // 현재 강 (ex: 8, 4, 2)
        int currentRoundOrder,        // 현재 강 내 진행 순서 (ex: 1, 2)
        boolean isTournamentFinished  // 토너먼트가 완전히 종료되었는지 여부
) {
}