package com.team.moodit.domain.feedback;

import com.team.moodit.storage.db.core.FeedbackEntity;
import com.team.moodit.storage.db.core.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackManager {
    private final FeedbackRepository feedbackRepository;

    public Long submit(Long userId, Long userMissionId, NewFeedback newFeedback) {
        return feedbackRepository.save(
                new FeedbackEntity(
                        userMissionId,
                        userId,
                        newFeedback.getSatisfactionScore(),
                        newFeedback.getDissatisfactionReason()
                )
        ).getId();
    }
}
