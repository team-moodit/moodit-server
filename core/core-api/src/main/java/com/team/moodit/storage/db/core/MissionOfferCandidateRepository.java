package com.team.moodit.storage.db.core;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionOfferCandidateRepository extends JpaRepository<MissionOfferCandidateEntity, Long> {
    List<MissionOfferCandidateEntity> findByOfferId(Long missionOfferId);
}
