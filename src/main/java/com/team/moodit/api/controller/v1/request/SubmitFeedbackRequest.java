package com.team.moodit.api.controller.v1.request;

import com.team.moodit.domain.feedback.NewFeedback;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;

public record SubmitFeedbackRequest(
        Double satisfactionScore,
        String dissatisfactionReason
) {
    public SubmitFeedbackRequest {
        if (satisfactionScore == null || satisfactionScore < 1 || satisfactionScore > 5) throw new ApiException(ErrorType.INVALID_REQUEST);
        if (satisfactionScore < 3 && dissatisfactionReason.isBlank()) throw new ApiException(ErrorType.FEEDBACK_REQUIRED_REASON);
    }

    public NewFeedback toNewFeedback() {
        return new NewFeedback(
                this.satisfactionScore,
                this.dissatisfactionReason
        );
    }
}
