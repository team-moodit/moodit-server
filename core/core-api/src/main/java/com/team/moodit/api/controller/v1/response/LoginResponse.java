package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.auth.LoginResult;

public record LoginResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        boolean isNewUser
) {
    public static LoginResponse of(LoginResult loginResult) {
        return new LoginResponse(
                loginResult.getUserId(),
                loginResult.getAccessToken(),
                loginResult.getRefreshToken(),
                loginResult.isNewUser()
        );
    }
}
