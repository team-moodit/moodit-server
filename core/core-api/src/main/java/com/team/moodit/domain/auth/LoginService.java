package com.team.moodit.domain.auth;

import com.team.moodit.storage.db.core.UserAuthIdentityEntity;
import com.team.moodit.storage.db.core.UserAuthIdentityRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final KakaoLoginHandler kakaoLoginHandler;
    private final SocialLoginHandler socialLoginHandler;
    private final TokenManager tokenManager;

    // TODO: 임시
    private final UserAuthIdentityRepository userAuthIdentityRepository;

    public LoginResult loginWithKakao(
            String kakaoAccessToken
    ) {
        SocialUserPrivacy profile = kakaoLoginHandler.getProfile(kakaoAccessToken);
        Optional<UserAuthIdentityEntity> existingUser = userAuthIdentityRepository.findByProviderTypeAndProviderUserId(
                profile.getProviderType(),
                profile.getProviderUserId()
        );

        AuthUser authUser = socialLoginHandler.authenticateSocialUser(profile);

        IssuedToken issuedToken = tokenManager.issue(authUser.getId(), authUser.getRole());

        return new LoginResult(
                authUser.getId(),
                issuedToken.getAccessToken(),
                issuedToken.getRefreshToken(),
                existingUser.isEmpty()
        );
    }
}
