package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchVoteCandidateRepository extends JpaRepository<MatchVoteCandidateEntity, Long> {

    List<MatchVoteCandidateEntity> findByMatchIdAndRoundNumberOrderByDisplayOrderAsc(
            Long matchId,
            Integer roundNumber
    );

    List<MatchVoteCandidateEntity> findAllByMatchIdOrderByRoundNumberAscDisplayOrderAsc(
            Long matchId
    );

    void deleteByMatchId(Long matchId);
}