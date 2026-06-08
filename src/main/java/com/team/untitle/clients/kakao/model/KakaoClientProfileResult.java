package com.team.untitle.clients.kakao.model;

import com.team.untitle.clients.kakao.KakaoProfileResponse;

public record KakaoClientProfileResult(
        String id
) {
    public static KakaoClientProfileResult of(KakaoProfileResponse response) {
        return new KakaoClientProfileResult(
                response.id()
        );
    }
}
