package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.Getter;

import java.util.Optional;

@Getter
public class RealMatchUp extends MatchUp {
    private Long candidateAId;
    private Long candidateBId;
    private Long winnerId = null; // 대결 시엔 승자 없음

    public RealMatchUp(Long matchId, int roundNumber, Long candidateAId, Long candidateBId) {
        super(matchId, roundNumber, MatchUpState.NEED_VOTE);
        if (candidateAId == null || candidateBId == null) {
            throw new ApiException(ErrorType.INVALID_MATCH_UP_CANDIDATE);
        }
        if (candidateAId.equals(candidateBId)) {
            throw new ApiException(ErrorType.INVALID_MATCH_UP_SAME_CANDIDATE);
        }

        this.candidateAId = candidateAId;
        this.candidateBId = candidateBId;
    }

    public Optional<Long> getWinnerId() {
        return Optional.ofNullable(winnerId);
    }
}
