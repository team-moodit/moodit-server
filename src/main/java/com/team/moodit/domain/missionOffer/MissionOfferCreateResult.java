package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.enums.PreferenceResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MissionOfferCreateResult {
    private MissionOffer missionOffer;
    private PreferenceResultType preferenceResultType;
    private Long assignedMissionId;

    public static MissionOfferCreateResult selection(MissionOffer missionOffer, PreferenceResultType preferenceResultType) {
        return new MissionOfferCreateResult(missionOffer, preferenceResultType, null);
    }

    public static MissionOfferCreateResult assigned(MissionOffer missionOffer, PreferenceResultType preferenceResultType, Long assignedMissionId) {
        return new MissionOfferCreateResult(missionOffer, preferenceResultType, assignedMissionId);
    }

    public static MissionOfferCreateResult of(MissionOffer missionOffer, PreferenceResultType preferenceResultType, Long assignedMissionId) {
        if (assignedMissionId == null) {
            return selection(missionOffer, preferenceResultType);
        }
        return assigned(missionOffer, preferenceResultType, assignedMissionId);
    }
}
