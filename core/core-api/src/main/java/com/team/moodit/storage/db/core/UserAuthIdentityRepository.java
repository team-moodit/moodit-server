package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.SocialProviderType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthIdentityRepository extends JpaRepository<UserAuthIdentityEntity, Long> {
    Boolean existsByProviderTypeAndProviderUserIdAndStatus(SocialProviderType providerType, String providerUserId, EntityStatus status);
    Optional<UserAuthIdentityEntity> findByProviderTypeAndProviderUserIdAndStatus(SocialProviderType providerType, String providerUserId, EntityStatus status);
    Optional<UserAuthIdentityEntity> findByUserIdAndStatus(Long userId, EntityStatus status);
}
