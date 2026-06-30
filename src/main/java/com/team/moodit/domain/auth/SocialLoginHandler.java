package com.team.moodit.domain.auth;

import com.team.moodit.domain.user.User;
import com.team.moodit.domain.user.UserManager;
import com.team.moodit.domain.user.UserReader;
import com.team.moodit.storage.db.core.UserAuthIdentityRepository;
import com.team.moodit.storage.db.core.UserPrivacyEntity;
import com.team.moodit.storage.db.core.UserPrivacyRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SocialLoginHandler {
    private final UserAuthIdentityRepository userAuthIdentityRepository;
    private final UserPrivacyRepository userPrivacyRepository;
    private final UserManager userManager;
    private final UserReader userReader;

    @Transactional
    public AuthUser authenticateSocialUser(SocialUserPrivacy socialUserPrivacy) {
        Boolean isNewUser = userAuthIdentityRepository.existsByProviderTypeAndProviderUserId(
                socialUserPrivacy.getProviderType(),
                socialUserPrivacy.getProviderUserId()
        );

        Long userId;
        if (isNewUser) {
            userId = userManager.createSocialUser(socialUserPrivacy);
            userPrivacyRepository.save(
                    new UserPrivacyEntity(
                            userId,
                            null,
                            socialUserPrivacy.getProviderUserEmail()
                    )
            );
        } else {
            userId = userAuthIdentityRepository.findByProviderTypeAndProviderUserId(
                    socialUserPrivacy.getProviderType(),
                    socialUserPrivacy.getProviderUserId()
            ).orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND)).getUserId();
        }

        User found = userReader.getUser(userId);
        return new AuthUser(
                found.getId(),
                found.getRole()
        );
    }
}
