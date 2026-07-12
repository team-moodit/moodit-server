package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.enums.PreferenceResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionOfferCreateResult {
    private MissionOffer missionOffer;
    private PreferenceResultType preferenceResultType;
}
