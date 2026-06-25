package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchVoteRepository extends JpaRepository<MatchVoteEntity, Long> {
    @Query(value = "SELECT * FROM match_vote ORDER BY RANDOM() LIMIT 4", nativeQuery = true)
    List<MatchVoteEntity> findRandomVotes();
}
