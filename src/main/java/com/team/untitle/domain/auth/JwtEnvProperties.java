package com.team.untitle.domain.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtEnvProperties(
    String secretKey,
    Long accessTokenExpireTime,
    Long refreshTokenExpireTime
) {
}
