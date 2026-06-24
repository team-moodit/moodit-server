package com.team.moodit.domain.missionOffer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MissionOfferCreateResult {
    private MissionOffer missionOffer;
    private Long userMissionId;

    public static MissionOfferCreateResult needsSelection(MissionOffer missionOffer) {
        return new MissionOfferCreateResult(missionOffer, null);
    }

    public static MissionOfferCreateResult accepted(MissionOffer missionOffer, Long userMissionId) {
        return new MissionOfferCreateResult(missionOffer, userMissionId);
    }
}
