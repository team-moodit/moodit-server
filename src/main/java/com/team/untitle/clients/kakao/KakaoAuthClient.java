package com.team.untitle.clients.kakao;

import com.team.untitle.clients.kakao.model.KakaoClientTokenResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoAuthClient {
    @Value("${kakao-auth.client-id}") private String clientId;
    @Value("${kakao-auth.login.redirect-uri}") private String redirectUri;
    @Value("${kakao-auth.login.client-secret}") private String clientSecret;

    private final KakaoAuthApi kakaoAuthApi;

    public KakaoClientTokenResult getAccessToken(String code) {
        return kakaoAuthApi.getAccessToken(
                clientId,
                redirectUri,
                code,
                clientSecret
        ).toResult();
    }
}
