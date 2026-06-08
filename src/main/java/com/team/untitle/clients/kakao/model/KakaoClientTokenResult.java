package com.team.untitle.clients.kakao.model;

import com.team.untitle.clients.kakao.KakaoTokenResponse;

public record KakaoClientTokenResult(
        String accessToken,
        String refreshToken
) {
    public static KakaoClientTokenResult of(KakaoTokenResponse response) {
        return new KakaoClientTokenResult(
                response.accessToken(),
                response.refreshToken()
        );
    }
}
