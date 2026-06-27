package com.team.moodit.domain.auth;

import com.team.moodit.clients.kakao.KakaoApiClient;
import com.team.moodit.clients.kakao.model.KakaoClientProfileResult;
import com.team.moodit.domain.enums.SocialProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoLoginHandler {
    private final KakaoApiClient kakaoApiClient;

    public SocialProfile getProfile(String kakaoAccessToken) {
        KakaoClientProfileResult kakaoProfile = kakaoApiClient.getProfile(kakaoAccessToken);

        return new SocialProfile(
                SocialProviderType.KAKAO,
                kakaoProfile.id(),
                kakaoProfile.email(),
                kakaoProfile.name()
        );
    }
}
