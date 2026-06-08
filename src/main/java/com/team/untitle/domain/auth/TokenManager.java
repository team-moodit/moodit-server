package com.team.untitle.domain.auth;

import com.team.untitle.domain.enums.UserRole;
import com.team.untitle.support.error.ApiException;
import com.team.untitle.support.error.ErrorType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class TokenManager {
    private final JwtEnvProperties jwtEnvProperties;
    private final SecretKey signingKey;

    public TokenManager(JwtEnvProperties jwtEnvProperties) {
        this.jwtEnvProperties = jwtEnvProperties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtEnvProperties.secretKey()));
    }

    public IssuedToken issue(Long userId, UserRole userRole) {
        Long accessTokenExpireTime = jwtEnvProperties.accessTokenExpireTime();
        Long refreshTokenExpireTime = jwtEnvProperties.refreshTokenExpireTime();

        return new IssuedToken(
                createAccessToken(userId, userRole, accessTokenExpireTime),
                createRefreshToken(userId, refreshTokenExpireTime)
        );
    }

    private String createAccessToken(Long userId, UserRole userRole, Long expireTime) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + expireTime))
                .claim("tokenType", "ACCESS")
                .claim("role", userRole.name()) // 필요 시 Claim 추가 가능
                .signWith(signingKey)
                .compact();
    }

    private String createRefreshToken(Long userId, Long expireTime) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + expireTime))
                .claim("tokenType", "REFRESH")
                .signWith(signingKey)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorType.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorType.INVALID_TOKEN);
        }
    }
}
