package com.team.untitle.support.auth;

import com.team.untitle.domain.auth.TokenManager;
import com.team.untitle.support.error.ApiException;
import com.team.untitle.support.error.ErrorType;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class ApiUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenManager tokenManager;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(ApiUser.class);
    }

    @Override
    public ApiUser resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) throw new ApiException(ErrorType.INVALID_REQUEST);

        String token = resolveToken(request);
        if (token == null || token.isBlank()) throw new ApiException(ErrorType.INVALID_TOKEN);

        Claims claims = tokenManager.getClaims(token);
        if (!"ACCESS".equals(claims.get("tokenType"))) throw new ApiException(ErrorType.INVALID_TOKEN);

        return new ApiUser(
                Long.parseLong(claims.getSubject())
        );
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
