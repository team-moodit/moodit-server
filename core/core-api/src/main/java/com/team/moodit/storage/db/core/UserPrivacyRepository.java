package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.EntityStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPrivacyRepository extends JpaRepository<UserPrivacyEntity, Long> {
    Optional<UserPrivacyEntity> findByUserIdAndStatus(Long userId, EntityStatus status);
}
