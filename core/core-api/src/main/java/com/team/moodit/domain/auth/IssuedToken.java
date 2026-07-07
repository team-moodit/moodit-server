package com.team.moodit.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssuedToken {
    private String accessToken;
    private String refreshToken;
}
