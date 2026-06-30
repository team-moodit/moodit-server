package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.response.UserPrivacyInfoResponse;
import com.team.moodit.domain.user.UserPrivacy;
import com.team.moodit.domain.user.UserService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SettingController {
    private final UserService userService;

    @GetMapping("/v1/settings/privacy/info")
    public ApiResponse<UserPrivacyInfoResponse> getUserPrivacyInfo(
            ApiUser apiUser
    ) {
        UserPrivacy userPrivacy = userService.getUserPrivacy(apiUser.getId());
        return ApiResponse.success(UserPrivacyInfoResponse.of(userPrivacy));
    }
}
