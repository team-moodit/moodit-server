package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.UserMissionState;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMissionRepository extends JpaRepository<UserMissionEntity, Long> {
    Optional<UserMissionEntity> findByIdAndUserIdAndStatus(Long id, Long userId, EntityStatus status);
    Page<UserMissionEntity> findByUserIdAndStateAndStatusOrderByIdDesc(Long userId, UserMissionState state, EntityStatus status, Pageable pageable);
    Optional<UserMissionEntity> findByMissionOfferIdAndStatus(Long missionOfferId, EntityStatus status);
    long countByUserIdAndStateAndStatus(Long userId, UserMissionState state, EntityStatus status);
}
