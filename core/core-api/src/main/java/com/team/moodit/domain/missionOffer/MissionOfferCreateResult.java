package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.enums.PreferenceResultType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MissionOfferCreateResult {
    private MissionOffer missionOffer;
    private PreferenceResultType preferenceResultType;
    private Long assignedMissionId;

    private static MissionOfferCreateResult assigned(MissionOffer missionOffer, PreferenceResultType preferenceResultType, Long assignedMissionId) {
        return new MissionOfferCreateResult(missionOffer, preferenceResultType, assignedMissionId);
    }

    public static MissionOfferCreateResult of(MissionOffer missionOffer, PreferenceResultType preferenceResultType, Long assignedMissionId) {
        if (assignedMissionId == null) {
            return new MissionOfferCreateResult(missionOffer, preferenceResultType, null);
        }
        return assigned(missionOffer, preferenceResultType, assignedMissionId);
    }
}
