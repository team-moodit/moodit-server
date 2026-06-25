package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchUpState;
import lombok.Getter;

@Getter
public class AutoPassMatch extends MatchUp {
    private Long candidateId; // 부전승자는 1명뿐
    private Long winnerId;    // 승자가 자기 자신

    public AutoPassMatch(Long matchId, int roundNumber, Long candidateId) {
        super(matchId, roundNumber, MatchUpState.SKIPPED);
        this.candidateId = candidateId;
        this.winnerId = candidateId;
    }
}
