package com.team.moodit.api.assembler;

import com.team.moodit.api.controller.v1.response.MissionOfferResponse;
import com.team.moodit.domain.missionOffer.MissionOffer;
import com.team.moodit.domain.missionOffer.MissionOfferService;
import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionOfferAssembler {
    private final MissionOfferService missionOfferService;

    public MissionOfferResponse createMissionOffer(ApiUser apiUser, Long matchId) {
        MissionOffer missionOffer = missionOfferService.createOffer(apiUser, matchId);
        // TODO: 추후 UserMissionId 조회 후 조합
        return MissionOfferResponse.of(missionOffer);
    }
}
