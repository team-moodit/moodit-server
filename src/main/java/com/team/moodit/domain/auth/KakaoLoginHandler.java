package com.team.moodit.domain.auth;

import com.team.moodit.clients.kakao.KakaoApiClient;
import com.team.moodit.clients.kakao.KakaoAuthClient;
import com.team.moodit.clients.kakao.model.KakaoClientProfileResult;
import com.team.moodit.clients.kakao.model.KakaoClientTokenResult;
import com.team.moodit.domain.enums.SocialProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoLoginHandler {
    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;

    public SocialProfile getProfile(String code) {
        KakaoClientTokenResult token = kakaoAuthClient.getAccessToken(code);
        KakaoClientProfileResult kakaoProfile = kakaoApiClient.getProfile(token.accessToken());

        return new SocialProfile(
                SocialProviderType.KAKAO,
                kakaoProfile.id(),
                kakaoProfile.email()
        );
    }
}
