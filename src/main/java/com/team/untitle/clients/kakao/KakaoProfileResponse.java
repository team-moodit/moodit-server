package com.team.untitle.clients.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.untitle.clients.kakao.model.KakaoClientProfileResult;

public record KakaoProfileResponse(
        String id,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    KakaoClientProfileResult toResult() {
        return KakaoClientProfileResult.of(this);
    }

    record KakaoAccount(
            // TODO: 추후 추가 필드 필요시 사용자 정보 추가될 예정
    ) {
    }
}
