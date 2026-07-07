package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class MatchUpStart {
    private final Long matchUpId; // 🎯 1. 누락되었던 대진표 식별자 필드 추가!
    private final String tournamentTitle;
    private final int totalRounds;
    private final int currentRound;
    private final String roundName;
    private final boolean isTournamentCompleted;
    private final Long candidateAId;
    private final String candidateAUrl;
    private final Long candidateBId;
    private final String candidateBUrl;
    private final List<MatchVoteCandidateEntity> reasons;

    // 정적 팩토리 메서드도 신규 필드 규격에 맞게 매핑 (종료 시 ID는 null)
    public static MatchUpStart createCompleted(String title) {
        return new MatchUpStart(null, title, 0, 0, "종료", true, null, null, null, null, List.of());
    }


    public Long getMatchUpId() {
        return this.matchUpId;
    }
}