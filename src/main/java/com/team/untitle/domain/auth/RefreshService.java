package com.team.untitle.domain.auth;

import com.team.untitle.domain.User;
import com.team.untitle.domain.UserReader;
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
    private final UserReader userReader;

    public IssuedToken refresh(String refreshToken) {
        Claims tokenClaims = tokenManager.getClaims(refreshToken);
        if (tokenClaims == null) throw new ApiException(ErrorType.INVALID_TOKEN);
        if (!"REFRESH".equals(tokenClaims.get("tokenType"))) throw new ApiException(ErrorType.INVALID_TOKEN);

        long userId = Long.parseLong(tokenClaims.getSubject());

        User user = userReader.getUser(userId);
        return tokenManager.issue(userId, user.getRole());
    }
}
