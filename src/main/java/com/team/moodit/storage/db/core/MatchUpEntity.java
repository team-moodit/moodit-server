package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.domain.match.AutoPassMatch;
import com.team.moodit.domain.match.MatchUp;
import com.team.moodit.domain.match.RealMatchUp;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="`match_up`")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchUpEntity extends BaseNoStatusEntity {
    private Long matchId;
    private int roundNumber;
    private Long candidateAId;
    private Long candidateBId;
    private Long winnerId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private MatchUpState state;

    // 1. 실제 대결용 private 생성자
    private MatchUpEntity(Long matchId, int roundNumber, Long candidateAId, Long candidateBId) {
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.candidateAId = candidateAId;
        this.candidateBId = candidateBId;
        this.state = MatchUpState.NEED_VOTE;
    }

    // 2. 부전승용 private 생성자
    private MatchUpEntity(Long matchId, int roundNumber, Long candidateId) {
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.candidateAId = candidateId;
        this.winnerId = candidateId;
        this.state = MatchUpState.SKIPPED;
    }

    // 3. of 메서드는 이 내부 생성자들을 호출하도록 변경
    public static MatchUpEntity of(MatchUp matchUp) {
        if (matchUp instanceof RealMatchUp real) {
            return new MatchUpEntity(real.getMatchId(), real.getRoundNumber(), real.getCandidateAId(), real.getCandidateBId());
        }
        if (matchUp instanceof AutoPassMatch autoPass) {
            return new MatchUpEntity(autoPass.getMatchId(), autoPass.getRoundNumber(), autoPass.getCandidateId());
        }
        throw new ApiException(ErrorType.INVALID_MATCH_UP_TYPE);
    }



}
