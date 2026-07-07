package com.team.moodit.domain.missionOffer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OfferAcceptAction {
    private Long offerId;
    private Long candidateId;
}
