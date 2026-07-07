package com.team.moodit.storage.db.core;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Optional<ReviewEntity> findByUserIdAndUserMissionId(Long userId, Long userMissionId);
    List<ReviewEntity> findByUserId(Long userId);

    List<ReviewEntity> findByUserMissionIdIn(Collection<Long> userMissionIds);
    Optional<ReviewEntity> findByUserMissionId(Long userMissionId);
}
