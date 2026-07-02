package com.team.moodit.storage.db.core;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    Optional<FeedbackEntity> findByUserIdAndUserMissionId(Long userId, Long userMissionId);
    List<FeedbackEntity> findByUserMissionIdIn(List<Long> userMissionIds);
    long countByUserId(Long userId);

    @Query(
        """
        SELECT AVG(feedback.satisfactionScore)
        FROM FeedbackEntity feedback
        WHERE feedback.userId = :userId
        """
    )
    Double averageSatisfactionScoreByUserId(Long userId);
}
