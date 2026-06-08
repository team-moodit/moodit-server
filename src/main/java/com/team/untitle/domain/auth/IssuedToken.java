package com.team.untitle.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssuedToken {
    private String accessToken;
    private String refreshToken;
}
