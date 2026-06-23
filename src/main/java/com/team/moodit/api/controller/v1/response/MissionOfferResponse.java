package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.MissionOfferState;
import com.team.moodit.domain.missionOffer.MissionCandidate;
import com.team.moodit.domain.missionOffer.MissionOffer;
import java.util.Comparator;
import java.util.List;

public record MissionOfferResponse(
        List<MissionOfferItemResponse> items,
        MissionOfferState state,
        Long userMissionId
) {
    public static MissionOfferResponse of(
            MissionOffer missionOffer
    ) {
        return new MissionOfferResponse(
                missionOffer.getCandidates().stream()
                        .sorted(Comparator.comparing(MissionCandidate::getDisplayOrder))
                        .map(it ->
                                new MissionOfferItemResponse(
                                        it.getId(),
                                        it.getTitle()
                                )
                        ).toList(),
                missionOffer.getState(),
                null
        );
    }

    record MissionOfferItemResponse(
            Long id,
            String title
    ) {
    }
}
