package com.team.moodit.api.controller.v1.request;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;

public record CreateMissionOfferRequest(
        Long matchId
) {
    public CreateMissionOfferRequest {
        if (matchId == null) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
