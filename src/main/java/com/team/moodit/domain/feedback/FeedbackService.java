package com.team.moodit.domain.feedback;

import com.team.moodit.support.auth.ApiUser;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackPolicyValidator feedbackPolicyValidator;
    private final FeedbackManager feedbackManager;
    private final FeedbackFinder feedbackFinder;

    public Long submit(ApiUser apiUser, Long userMissionId, NewFeedback newFeedback) {
        feedbackPolicyValidator.validateNew(apiUser.getId(), userMissionId);
        return feedbackManager.submit(apiUser.getId(), userMissionId, newFeedback);
    }

    public Map<Long, Double> scores(List<Long> missionIds) {
        return feedbackFinder.scoreByMissionIds(missionIds);
    }
}
