package com.team.moodit.domain.missionOffer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MissionOfferCreateResult {
    private MissionOffer missionOffer;
    private Long assignedMissionId;

    public static MissionOfferCreateResult selection(MissionOffer missionOffer) {
        return new MissionOfferCreateResult(missionOffer, null);
    }

    public static MissionOfferCreateResult assigned(MissionOffer missionOffer, Long assignedMissionId) {
        return new MissionOfferCreateResult(missionOffer, assignedMissionId);
    }

    public static MissionOfferCreateResult of(MissionOffer missionOffer, Long assignedMissionId) {
        if (assignedMissionId == null) {
            return selection(missionOffer);
        }
        return assigned(missionOffer, assignedMissionId);
    }
}
