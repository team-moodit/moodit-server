package com.team.moodit.api.controller.v1.request;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;

public record CreateMissionOfferRequest(
        Long matchResultId
) {
    public CreateMissionOfferRequest {
        if (matchResultId == null) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
