package com.team.untitle.domain;

import com.team.untitle.domain.enums.SocialProviderType;
import com.team.untitle.storage.db.core.UserAuthIdentityEntity;
import com.team.untitle.storage.db.core.UserAuthIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAuthIdentityFinder {
    private final UserAuthIdentityRepository userAuthIdentityRepository;

    public Long findUserIdOrNull(SocialProviderType provider, String providerUserId) {
        return userAuthIdentityRepository.findByProviderTypeAndProviderUserId(provider, providerUserId)
                .map(UserAuthIdentityEntity::getUserId)
                .orElse(null);
    }
}
