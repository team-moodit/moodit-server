package com.team.moodit.api.controller.v1.request;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;

public record TokenRefreshRequest(
        String refreshToken
) {
    public TokenRefreshRequest {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
