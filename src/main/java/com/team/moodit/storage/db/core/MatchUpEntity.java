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




    /**
     * [요구사항 5, 6, 7번] 득표수 반영 및 승자 확정 처리
     */
    public void updateWinner(Long selectedPhotoId) {
        this.winnerId = selectedPhotoId;
        this.state = MatchUpState.COMPLETED; // 상태를 완료(또는 프로젝트 컨벤션에 맞는 완료 상태)로 변경!
    }

    /**
     * Manager의 .filter(m -> !m.isVoted()) 가 작동할 수 있도록 상태 확인 메서드 제공
     */
    /**
     * [요구사항 2, 3번 검증] 선택한 이미지가 현재 대진의 후보가 맞는지 검증
     */
    public void validateCandidate(Long selectedPhotoId) {
        //  공백이나 상태값 버그에 휘둘리지 않도록, winnerId가 진짜 채워져 있는지만 정석대로 검사합니다!
        if (this.winnerId != null) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        // 선택한 사진이 후보 A도 아니고 후보 B도 아니면 예외 처리
        if (!selectedPhotoId.equals(this.candidateAId) && !selectedPhotoId.equals(this.candidateBId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }

    /**
     * Manager의 .filter(m -> !m.isVoted()) 가 작동할 수 있도록 상태 확인 메서드 제공
     */
    public boolean isVoted() {
        // 승자 ID(winnerId)가 존재하면 무조건 투표가 끝난 경기입니다.
        return this.winnerId != null;
    }




}
