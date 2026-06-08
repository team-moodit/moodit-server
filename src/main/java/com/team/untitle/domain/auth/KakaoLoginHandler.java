package com.team.untitle.domain.auth;

import com.team.untitle.clients.kakao.KakaoApiClient;
import com.team.untitle.clients.kakao.KakaoAuthClient;
import com.team.untitle.clients.kakao.model.KakaoClientProfileResult;
import com.team.untitle.clients.kakao.model.KakaoClientTokenResult;
import com.team.untitle.domain.enums.SocialProviderType;
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
                kakaoProfile.id()
        );
    }
}
