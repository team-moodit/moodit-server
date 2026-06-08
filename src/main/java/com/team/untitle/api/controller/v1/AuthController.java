package com.team.untitle.api.controller.v1;

import com.team.untitle.api.controller.v1.request.KakaoLoginRequest;
import com.team.untitle.api.controller.v1.response.LoginResponse;
import com.team.untitle.domain.auth.LoginResult;
import com.team.untitle.domain.auth.LoginService;
import com.team.untitle.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final LoginService loginService;

    @PostMapping("/v1/auth/kakao-login")
    public ApiResponse<LoginResponse> loginWithKakao(
            @RequestBody KakaoLoginRequest request
    ) {
        LoginResult result = loginService.loginWithKakao(request.code());
        return ApiResponse.success(new LoginResponse(
                result.getUserId(),
                result.getAccessToken(),
                result.getRefreshToken()
        ));
    }
}
