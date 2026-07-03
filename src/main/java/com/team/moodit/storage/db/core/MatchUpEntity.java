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
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "match_up", indexes = {
        @Index(name = "idx_match_id", columnList = "matchId"),         // 토너먼트 전체 조회 시 성능 향상
        @Index(name = "idx_match_id_round", columnList = "matchId, roundNumber") // 특정 라운드 경기 조회 시 최적화
})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchUpEntity extends BaseNoStatusEntity {

    @Column(nullable = false)
    private Long matchId;
    private int roundNumber;
    private Long candidateAId;
    private Long candidateBId;
    private Long winnerId;

    @Enumerated(EnumType.STRING)
    private MatchUpState state;

    @Version
    private Long version;

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

    // 3. 정적 팩토리 메서드
    public static MatchUpEntity of(MatchUp matchUp) {
        if (matchUp instanceof RealMatchUp real) {
            return new MatchUpEntity(real.getMatchId(), real.getRoundNumber(), real.getCandidateAId(), real.getCandidateBId());
        }
        if (matchUp instanceof AutoPassMatch autoPass) {
            return new MatchUpEntity(autoPass.getMatchId(), autoPass.getRoundNumber(), autoPass.getCandidateId());
        }
        throw new ApiException(ErrorType.INVALID_MATCH_UP_TYPE);
    }

    /**
     * 득표수 반영 및 승자 확정 처리
     */
    public void updateWinner(Long selectedPhotoId) {
        this.winnerId = selectedPhotoId;
        this.state = MatchUpState.COMPLETED;
    }

    /**
     * 선택한 이미지가 현재 대진의 후보가 맞는지 검증 (중복 투표 및 부전승 투표 차단)
     */
    public void validateCandidate(Long selectedPhotoId) {
        if (this.state == MatchUpState.SKIPPED) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        if (this.winnerId != null) {
            if (isSameWinner(selectedPhotoId)) {
                return;
            }

            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        if (!selectedPhotoId.equals(this.candidateAId) && !selectedPhotoId.equals(this.candidateBId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }

    public boolean isSameWinner(Long selectedPhotoId) {
        return this.winnerId != null && this.winnerId.equals(selectedPhotoId);
    }
    /**
     * 해당 경기가 이미 투표 완료되었는지 여부 반환
     */
    public boolean isVoted() {
        return this.state == MatchUpState.COMPLETED
                || this.state == MatchUpState.SKIPPED;
    }
}
