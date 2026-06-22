package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionOfferRepository extends JpaRepository<MissionOfferEntity, Long> {
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);
}
