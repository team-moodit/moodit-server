package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchVoteCandidateEntity; // 1. import 문을 새로 바꾼 엔티티로 변경
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class MatchUpStart {
    private final String tournamentTitle;
    private final int totalRounds;
    private final int currentRound;
    private final String roundName;
    private final boolean isTournamentCompleted; // 아까 Reader에서 추가했던 이 필드도 꼭 넣어주세요!
    private final Long candidateAId;
    private final String candidateAUrl;
    private final Long candidateBId;
    private final String candidateBUrl;
    private final List<MatchVoteCandidateEntity> reasons; // 2. 타입을 여기서 변경!

    public static MatchUpStart createCompleted(String title) {
        return new MatchUpStart(title, 0, 0, "종료", true, null, null, null, null, List.of());
    }

}