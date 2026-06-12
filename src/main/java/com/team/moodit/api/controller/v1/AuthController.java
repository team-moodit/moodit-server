package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.request.KakaoLoginRequest;
import com.team.moodit.api.controller.v1.response.AuthTokenResponse;
import com.team.moodit.api.controller.v1.response.LoginResponse;
import com.team.moodit.domain.auth.IssuedToken;
import com.team.moodit.domain.auth.LoginResult;
import com.team.moodit.domain.auth.LoginService;
import com.team.moodit.domain.auth.RefreshService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final LoginService loginService;
    private final RefreshService refreshService;

    @PostMapping("/v1/auth/kakao-login")
    public ApiResponse<LoginResponse> loginWithKakao(
            @RequestBody KakaoLoginRequest request
    ) {
        LoginResult result = loginService.loginWithKakao(request.accessToken());
        return ApiResponse.success(new LoginResponse(
                result.getUserId(),
                result.getAccessToken(),
                result.getRefreshToken()
        ));
    }

    @PostMapping("/v1/auth/refresh")
    public ApiResponse<AuthTokenResponse> refreshToken(
            @RequestBody String refreshToken
    ) {
        IssuedToken token = refreshService.refresh(refreshToken);
        return ApiResponse.success(new AuthTokenResponse(
                token.getAccessToken(),
                token.getRefreshToken()
        ));
    }

    @PostMapping("/v1/auth/logout")
    public ApiResponse<?> logout(
            ApiUser apiUser
    ) {
        // TODO: 로그아웃 구현을 위해 refresh token table 구성 필요 (임시 보류)
        return ApiResponse.success();
    }
}
