package com.team.untitle.storage.db.core;

import com.team.untitle.domain.enums.SocialProviderType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthIdentityRepository extends JpaRepository<UserAuthIdentityEntity, Long> {
    Optional<UserAuthIdentityEntity> findByProviderTypeAndProviderUserId(SocialProviderType provider, String providerUserId);
}
