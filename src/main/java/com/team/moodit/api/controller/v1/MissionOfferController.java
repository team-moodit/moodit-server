package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.request.CreateMissionOfferRequest;
import com.team.moodit.api.controller.v1.response.MissionOfferResponse;
import com.team.moodit.domain.missionOffer.MissionOffer;
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
    public ApiResponse<MissionOfferResponse> createMissionOffer(
//            ApiUser apiUser,
            @RequestBody CreateMissionOfferRequest request
    ) {
        MissionOffer missionOffer = missionOfferService.createOffer(1L, request.matchId());
        return ApiResponse.success(MissionOfferResponse.of(missionOffer));
    }
}
