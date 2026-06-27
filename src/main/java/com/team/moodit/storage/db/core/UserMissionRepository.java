package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.UserMissionState;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMissionRepository extends JpaRepository<UserMissionEntity, Long> {
    Optional<UserMissionEntity> findByIdAndUserId(Long id, Long userId);
    Optional<UserMissionEntity> findByIdAndUserIdAndState(Long id, Long userId, UserMissionState state);
    Page<UserMissionEntity> findByUserIdAndStateOrderByIdDesc(Long userId, UserMissionState state, Pageable pageable);
    Optional<UserMissionEntity> findByMissionOfferId(Long missionOfferId);

    @Query("""
        select userMission
        from UserMissionEntity userMission
        where userMission.userId = :userId
          and userMission.state = UserMissionState.COMPLETED
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
