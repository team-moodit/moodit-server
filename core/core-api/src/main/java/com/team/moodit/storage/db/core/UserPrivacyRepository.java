package com.team.moodit.storage.db.core;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPrivacyRepository extends JpaRepository<UserPrivacyEntity, Long> {
    Optional<UserPrivacyEntity> findByUserId(Long userId);
}
