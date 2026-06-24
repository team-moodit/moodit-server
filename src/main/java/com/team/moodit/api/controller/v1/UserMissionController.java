package com.team.moodit.api.controller.v1;

import com.team.moodit.api.assembler.UserMissionAssembler;
import com.team.moodit.api.controller.v1.response.UserMissionResponse;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserMissionController {
    private final UserMissionAssembler userMissionAssembler;

    @GetMapping("/v1/user-missions/{userMissionId}")
    public ApiResponse<UserMissionResponse> getUserMission(
//            ApiUser apiUser,
            @PathVariable Long userMissionId
    ) {
        return ApiResponse.success(userMissionAssembler.getUserMission(new ApiUser(1L), userMissionId));
    }
}
