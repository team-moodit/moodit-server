package com.team.untitle.domain.auth;

import com.team.untitle.domain.enums.UserRole;
import com.team.untitle.support.error.ApiException;
import com.team.untitle.support.error.ErrorType;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {
    private final TokenManager tokenManager;

    public IssuedToken refresh(String refreshToken) {
        Claims tokenClaims = tokenManager.getClaims(refreshToken);
        if (tokenClaims == null) throw new ApiException(ErrorType.INVALID_TOKEN);
        if (!"REFRESH".equals(tokenClaims.get("tokenType"))) throw new ApiException(ErrorType.INVALID_TOKEN);

        return tokenManager.issue(
                Long.parseLong(tokenClaims.getSubject()),
                UserRole.valueOf(tokenClaims.get("role").toString())
        );
    }
}
