package com.team.moodit.client.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

record KakaoProfileResponse(
        String id,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    KakaoClientProfileResult toResult() {
        return KakaoClientProfileResult.of(this);
    }

    record KakaoAccount(
            String email
    ) {
    }
}
