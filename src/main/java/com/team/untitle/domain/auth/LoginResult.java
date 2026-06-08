package com.team.untitle.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {
    private Long userId;
    private String accessToken;
    private String refreshToken;
}
