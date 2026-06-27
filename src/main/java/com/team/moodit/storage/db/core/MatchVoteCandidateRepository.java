package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchVoteCandidateRepository extends JpaRepository<MatchVoteCandidateEntity, Long> {
    //  GET /v1/matches/{matchId}/next-matchup 할 때 인덱스를 타며 빛을 발할 조회 메서드입니다.
    List<MatchVoteCandidateEntity> findByMatchIdAndRoundNumber(Long matchId, Integer roundNumber);
}