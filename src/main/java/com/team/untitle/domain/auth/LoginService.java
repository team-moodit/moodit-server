package com.team.untitle.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final KakaoLoginHandler kakaoLoginHandler;
    private final SocialLoginHandler socialLoginHandler;
    private final TokenManager tokenManager;

    public LoginResult loginWithKakao(
            String code
    ) {
        SocialProfile profile = kakaoLoginHandler.getProfile(code);
        AuthUser authUser = socialLoginHandler.loginOrSignup(profile);
        // TODO: 추후 유저 프로필 관련 정의가 나오면 UserProfile 저장 부분 추가 예정

        IssuedToken issuedToken = tokenManager.issue(authUser.getId(), authUser.getRole());

        return new LoginResult(
                authUser.getId(),
                issuedToken.getAccessToken(),
                issuedToken.getRefreshToken()
        );
    }
}
