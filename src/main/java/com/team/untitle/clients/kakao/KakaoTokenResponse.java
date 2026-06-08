package com.team.untitle.clients.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.untitle.clients.kakao.model.KakaoClientTokenResult;

/**
 * 카카오 외부 API로 부터 받아오는 Row API 응답용 객체
 */
public record KakaoTokenResponse(
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        String expiresIn,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("refresh_token_expires_in")
        String refreshTokenExpiresIn
) {
        KakaoClientTokenResult toResult() {
                return KakaoClientTokenResult.of(this);
        }
}
