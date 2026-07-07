package com.team.moodit.client.kakao;

public record KakaoClientProfileResult(
        String id,
        String email
) {
    static KakaoClientProfileResult of(KakaoProfileResponse response) {
        return new KakaoClientProfileResult(
                response.id(),
                response.kakaoAccount().email()
        );
    }
}
