package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.UserMissionState;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMissionRepository extends JpaRepository<UserMissionEntity, Long> {
    Optional<UserMissionEntity> findByIdAndUserId(Long id, Long userId);
    Optional<UserMissionEntity> findByIdAndUserIdAndState(Long id, Long userId, UserMissionState state);
    Page<UserMissionEntity> findByUserIdAndStateOrderByIdDesc(Long userId, UserMissionState state, Pageable pageable);
    Optional<UserMissionEntity> findByMissionOfferId(Long missionOfferId);
}
