package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MissionOfferState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionOfferRepository extends JpaRepository<MissionOfferEntity, Long> {
    Optional<MissionOfferEntity> findByIdAndState(Long id, MissionOfferState state);

    Optional<MissionOfferEntity> findByUserIdAndMatchResultId(Long userid, Long matchResultId);
}
