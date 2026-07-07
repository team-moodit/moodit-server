package com.team.moodit.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
}
