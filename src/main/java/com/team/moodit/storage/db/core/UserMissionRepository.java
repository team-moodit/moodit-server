package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.UserMissionState;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMissionRepository extends JpaRepository<UserMissionEntity, Long> {
    Optional<UserMissionEntity> findByIdAndUserIdAndStatus(Long id, Long userId, EntityStatus status);
    Optional<UserMissionEntity> findByIdAndUserIdAndStateAndStatus(Long id, Long userId, UserMissionState state, EntityStatus status);
    Page<UserMissionEntity> findByUserIdAndStateAndStatusOrderByIdDesc(Long userId, UserMissionState state, EntityStatus status, Pageable pageable);
    Optional<UserMissionEntity> findByMissionOfferIdAndStatus(Long missionOfferId, EntityStatus status);

    @Query("""
        select userMission
        from UserMissionEntity userMission
        where userMission.userId = :userId
          and userMission.state = UserMissionState.COMPLETED
          and userMission.status = EntityStatus.ACTIVE
          and exists (
              select 1
              from FeedbackEntity feedback
              where feedback.userMissionId = userMission.id
          )
        order by userMission.id desc
        """)
    Page<UserMissionEntity> findCompletedWithFeedback(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
