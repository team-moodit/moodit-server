package com.team.moodit.domain;

import com.team.moodit.domain.auth.SocialProfile;
import com.team.moodit.storage.db.core.UserProfileEntity;
import com.team.moodit.storage.db.core.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileManager {
    private final UserProfileRepository userProfileRepository;

    public Long createNew(Long userId, SocialProfile profile) {
        return userProfileRepository.save(
                new UserProfileEntity(
                        userId,
                        profile.getProviderUserEmail()
                )
        ).getId();
    }
}
