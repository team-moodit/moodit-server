package com.team.moodit.domain.feedback;

import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackPolicyValidator feedbackPolicyValidator;
    private final FeedbackManager feedbackManager;

    public Long submit(ApiUser apiUser, Long userMissionId, NewFeedback newFeedback) {
        feedbackPolicyValidator.validateNew(apiUser.getId(), userMissionId);
        return feedbackManager.submit(apiUser.getId(), userMissionId, newFeedback);
    }
}
