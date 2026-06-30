package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchUpState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchUpRepository extends JpaRepository<MatchUpEntity, Long> {

    // 1. 공통 조회 쿼리 (가장 자주 사용됨)
    @Query("SELECT m FROM MatchUpEntity m WHERE m.matchId = :matchId ORDER BY m.roundNumber ASC, m.id ASC")
    List<MatchUpEntity> findByMatchId(@Param("matchId") Long matchId);

    // 2. 상태별 첫 번째 경기 조회
    Optional<MatchUpEntity> findFirstByMatchIdAndState(Long matchId, MatchUpState state);

    // 3. 상태별 경기 카운트
    int countByMatchIdAndState(Long matchId, MatchUpState state);

    /**
     * [추가된 동시성 제어 쿼리]
     * 라운드 종료 판정 및 대진표 중복 생성(Phantom Read)을 방지하기 위한 비관적 쓰기 락 조회입니다.
     */


    @Query("SELECT mc FROM MatchVoteCandidateEntity mc WHERE mc.matchId = :matchId")
    List<MatchVoteCandidateEntity> findVotedLabelsByMatchId(@Param("matchId") Long matchId);

    void deleteByMatchId(Long matchId);
}