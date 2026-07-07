package com.team.moodit.support.auth;

import com.team.moodit.domain.auth.TokenManager;
import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.storage.db.core.UserRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
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
    private final UserRepository userRepository;

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

        long subject = Long.parseLong(claims.getSubject());
        return new ApiUser(
                userRepository.findByIdAndStatus(subject, EntityStatus.ACTIVE).orElseThrow(() -> new ApiException(ErrorType.INVALID_TOKEN)).getId()
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
