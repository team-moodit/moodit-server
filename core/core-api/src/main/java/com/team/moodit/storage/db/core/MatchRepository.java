package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    List<MatchEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<MatchEntity> findByUserIdAndState(Long id, MatchState state);

    Optional<MatchEntity> findByIdAndUserId(Long id, Long userId);
}
