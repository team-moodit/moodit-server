package com.team.moodit.clients.kakao.model;

import com.team.moodit.clients.kakao.KakaoProfileResponse;

public record KakaoClientProfileResult(
        String id,
        String email,
        String name
) {
    public static KakaoClientProfileResult of(KakaoProfileResponse response) {
        return new KakaoClientProfileResult(
                response.id(),
                response.kakaoAccount().email(),
                response.kakaoAccount().name()
        );
    }
}
