package com.team.untitle.api.controller.v1.response;

public record LoginResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {
}
