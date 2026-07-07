package com.team.moodit.domain.auth;

import com.team.moodit.client.kakao.KakaoClient;
import com.team.moodit.client.kakao.KakaoClientProfileResult;
import com.team.moodit.domain.enums.SocialProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoLoginHandler {
    private final KakaoClient kakaoClient;

    public SocialUserPrivacy getProfile(String kakaoAccessToken) {
        KakaoClientProfileResult kakaoProfile = kakaoClient.getProfile(kakaoAccessToken);

        return new SocialUserPrivacy(
                SocialProviderType.KAKAO,
                kakaoProfile.id(),
                kakaoProfile.email()
        );
    }
}
