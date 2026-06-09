package com.team.untitle.api.controller.v1.response;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken
) {
}
