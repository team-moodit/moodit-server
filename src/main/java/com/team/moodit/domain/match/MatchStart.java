package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchUpState;
import lombok.Getter;
import java.util.List;

@Getter
public class MatchStart {
    private final Long matchId;
    private final String title;
    private final MatchUpState state;
    private final Long candidateAId;
    private final String candidateAUrl;
    private final Long candidateBId;
    private final String candidateBUrl;
    private final int totalRounds;
    private final int currentRound;
    private final String roundName;

    // 📌 희연/호준님 컨벤션 반영: MatchReason 대신 MatchVoteResponse로 정합성 일치
    private final List<MatchVoteResponse> matchVotes;

    // 순수 생성자 조립 (팀원 DDD/Clean Code 룰 준수)
    public MatchStart(
            Long matchId,
            String title,
            MatchUpState state,
            Long candidateAId,
            String candidateAUrl,
            Long candidateBId,
            String candidateBUrl,
            int totalRounds,
            int currentRound,
            String roundName,
            List<MatchVoteResponse> matchVotes // 📌 파라미터 타입 매핑 완료
    ) {
        this.matchId = matchId;
        this.title = title;
        this.state = state;
        this.candidateAId = candidateAId;
        this.candidateAUrl = candidateAUrl;
        this.candidateBId = candidateBId;
        this.candidateBUrl = candidateBUrl;
        this.totalRounds = totalRounds;
        this.currentRound = currentRound;
        this.roundName = roundName;
        this.matchVotes = matchVotes;
    }
}