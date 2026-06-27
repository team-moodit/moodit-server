package com.team.moodit.storage.db.core;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    Optional<FeedbackEntity> findByUserIdAndUserMissionId(Long userId, Long userMissionId);
    List<FeedbackEntity> findByUserMissionIdIn(List<Long> userMissionIds);
}
