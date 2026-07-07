package com.team.moodit.api.controller.v1.request;

import com.team.moodit.domain.missionOffer.OfferAcceptAction;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;

public record AcceptMissionOfferRequest(
        Long offerId,
        Long candidateId
) {
    public AcceptMissionOfferRequest {
        if (offerId == null || offerId < 0) throw new ApiException(ErrorType.INVALID_REQUEST);
        if (candidateId == null || candidateId < 0) throw new ApiException(ErrorType.INVALID_REQUEST);
    }

    public OfferAcceptAction toOfferAcceptAction() {
        return new OfferAcceptAction(
                this.offerId,
                this.candidateId
        );
    }
}
