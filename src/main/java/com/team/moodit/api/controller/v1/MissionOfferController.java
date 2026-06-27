package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.request.AcceptMissionOfferRequest;
import com.team.moodit.api.controller.v1.request.CreateMissionOfferRequest;
import com.team.moodit.api.controller.v1.response.AcceptMissionOfferResponse;
import com.team.moodit.api.controller.v1.response.MissionOfferResponse;
import com.team.moodit.domain.missionOffer.MissionOfferCreateResult;
import com.team.moodit.domain.missionOffer.MissionOfferService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MissionOfferController {
    private final MissionOfferService missionOfferService;

    @PostMapping("/v1/mission-offers")
    public ApiResponse<MissionOfferResponse> getMissionOffer(
            ApiUser apiUser,
            @RequestBody CreateMissionOfferRequest request
    ) {
        MissionOfferCreateResult result = missionOfferService.getOrCreateOffer(apiUser, request.matchResultId());
        return ApiResponse.success(MissionOfferResponse.of(result));
    }

    @PostMapping("/v1/mission-offers/accept")
    public ApiResponse<AcceptMissionOfferResponse> acceptMissionOffer(
            ApiUser apiUser,
            @RequestBody AcceptMissionOfferRequest request
    ) {
        Long successId = missionOfferService.acceptOffer(
                apiUser,
                request.toOfferAcceptAction()
        );
        return ApiResponse.success(new AcceptMissionOfferResponse(successId));
    }
}
