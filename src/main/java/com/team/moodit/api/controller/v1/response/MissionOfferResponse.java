package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.MissionOfferState;
import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.domain.missionOffer.MissionCandidate;
import com.team.moodit.domain.missionOffer.MissionOfferCreateResult;
import java.util.Comparator;
import java.util.List;

public record MissionOfferResponse(
        Long offerId,
        PreferenceResultType preferenceResultType,
        List<MissionOfferItemResponse> items,
        MissionOfferState state,
        Long assignedMissionId
) {
    public static MissionOfferResponse of(
            MissionOfferCreateResult result
    ) {
        return new MissionOfferResponse(
                result.getMissionOffer().getId(),
                result.getPreferenceResultType(),
                result.getMissionOffer().getCandidates().stream()
                        .sorted(Comparator.comparing(MissionCandidate::getDisplayOrder))
                        .map(it ->
                                new MissionOfferItemResponse(
                                        it.getId(),
                                        it.getTitle()
                                )
                        ).toList(),
                result.getMissionOffer().getState(),
                result.getAssignedMissionId()
        );
    }

    record MissionOfferItemResponse(
            Long id,
            String title
    ) {
    }
}
