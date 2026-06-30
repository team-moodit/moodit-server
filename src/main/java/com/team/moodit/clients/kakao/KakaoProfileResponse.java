package com.team.moodit.clients.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.moodit.clients.kakao.model.KakaoClientProfileResult;

public record KakaoProfileResponse(
        String id,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    KakaoClientProfileResult toResult() {
        return KakaoClientProfileResult.of(this);
    }

    public record KakaoAccount(
            String email
    ) {
    }
}
