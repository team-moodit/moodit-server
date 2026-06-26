package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.response.UserProfileResponse;
import com.team.moodit.domain.user.UserProfile;
import com.team.moodit.domain.user.UserService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserProfileController {
    private final UserService userService;

    /**
     * 현재 사용자에게 활성화되어 있는 사용자 프로필을 조회
     */
    @GetMapping("/v1/user-profiles/active")
    public ApiResponse<UserProfileResponse> getActiveUserProfile(
            ApiUser apiUser
    ) {
        UserProfile profile = userService.getProfile(apiUser.getId());
        return ApiResponse.success(UserProfileResponse.of(profile));
    }

}
