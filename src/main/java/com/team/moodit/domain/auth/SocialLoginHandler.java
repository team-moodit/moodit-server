package com.team.moodit.domain.auth;

import com.team.moodit.domain.user.User;
import com.team.moodit.domain.user.UserAuthIdentityFinder;
import com.team.moodit.domain.user.UserManager;
import com.team.moodit.domain.user.UserProfileManager;
import com.team.moodit.domain.user.UserReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SocialLoginHandler {
    private final UserAuthIdentityFinder userAuthIdentityFinder;
    private final UserReader userReader;
    private final UserManager userManager;
    private final UserProfileManager userProfileManager;

    @Transactional
    public AuthUser loginOrSignup(SocialProfile profile) {
        Long userId = Optional.ofNullable(
                userAuthIdentityFinder.findUserIdOrNull(profile.getProvider(), profile.getProviderUserId())
        ).orElseGet(() -> {
            Long createdUserId = userManager.createSocialUser(profile);
            userProfileManager.createNew(createdUserId, profile);
            return createdUserId;
        });

        User found = userReader.getUser(userId);

        return new AuthUser(
                found.getId(),
                found.getRole()
        );
    }
}
