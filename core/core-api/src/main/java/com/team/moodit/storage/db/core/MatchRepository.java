package com.team.moodit.storage.db.core;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    List<MatchEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<MatchEntity> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    Optional<MatchEntity> findByIdAndUserId(Long id, Long userId);
}
