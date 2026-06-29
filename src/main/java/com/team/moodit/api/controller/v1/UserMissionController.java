package com.team.moodit.api.controller.v1;

import com.team.moodit.api.assembler.UserMissionAssembler;
import com.team.moodit.api.controller.v1.request.SubmitFeedbackRequest;
import com.team.moodit.api.controller.v1.response.UserMissionResponse;
import com.team.moodit.domain.feedback.FeedbackService;
import com.team.moodit.domain.userMission.UserMissionService;
import com.team.moodit.support.OffsetLimit;
import com.team.moodit.support.Page;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import com.team.moodit.support.response.DefaultIdResponse;
import com.team.moodit.support.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserMissionController {
    private final UserMissionAssembler userMissionAssembler;
    private final FeedbackService feedbackService;
    private final UserMissionService userMissionService;

    @GetMapping("/v1/user-missions")
    public ApiResponse<PageResponse<UserMissionResponse>> getUserMissions(
            ApiUser apiUser,
            @RequestParam UserMissionListType type,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        Page<UserMissionResponse> responses = userMissionAssembler.getUserMissions(
                apiUser,
                type,
                new OffsetLimit(offset, limit)
        );
        return ApiResponse.success(PageResponse.of(responses));
    }

    @GetMapping("/v1/user-missions/{userMissionId}")
    public ApiResponse<UserMissionResponse> getUserMission(
            ApiUser apiUser,
            @PathVariable Long userMissionId
    ) {
        return ApiResponse.success(userMissionAssembler.getUserMission(apiUser, userMissionId));
    }

    @PostMapping("/v1/user-missions/{userMissionId}/complete")
    public ApiResponse<DefaultIdResponse> completeUserMission(
            ApiUser apiUser,
            @PathVariable Long userMissionId
    ) {
        Long successId = userMissionService.completeUserMission(apiUser, userMissionId);
        return ApiResponse.success(new DefaultIdResponse(successId));
    }

    @PostMapping("/v1/user-missions/{userMissionId}/feedback")
    public ApiResponse<DefaultIdResponse> submitFeedback(
            ApiUser apiUser,
            @PathVariable Long userMissionId,
            @RequestBody SubmitFeedbackRequest request
    ) {
        Long successId = feedbackService.submit(apiUser, userMissionId, request.toNewFeedback());
        return ApiResponse.success(new DefaultIdResponse(successId));
    }
}
