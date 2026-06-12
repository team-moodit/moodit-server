package com.team.moodit.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final KakaoLoginHandler kakaoLoginHandler;
    private final SocialLoginHandler socialLoginHandler;
    private final TokenManager tokenManager;

    public LoginResult loginWithKakao(
            String kakaoAccessToken
    ) {
        SocialProfile profile = kakaoLoginHandler.getProfile(kakaoAccessToken);
        AuthUser authUser = socialLoginHandler.loginOrSignup(profile);

        IssuedToken issuedToken = tokenManager.issue(authUser.getId(), authUser.getRole());

        return new LoginResult(
                authUser.getId(),
                issuedToken.getAccessToken(),
                issuedToken.getRefreshToken()
        );
    }
}
