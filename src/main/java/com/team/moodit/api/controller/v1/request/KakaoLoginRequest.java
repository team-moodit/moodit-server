package com.team.moodit.api.controller.v1.request;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;

public record KakaoLoginRequest(
        String accessToken // 카카오 Access Token
) {
    public KakaoLoginRequest {
        if (accessToken == null || accessToken.isBlank()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
