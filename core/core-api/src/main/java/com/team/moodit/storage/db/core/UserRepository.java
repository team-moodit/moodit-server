package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.EntityStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByIdAndStatus(Long id, EntityStatus status);
}
