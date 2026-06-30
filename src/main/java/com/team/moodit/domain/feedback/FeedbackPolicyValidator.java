package com.team.moodit.domain.feedback;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.storage.db.core.FeedbackRepository;
import com.team.moodit.storage.db.core.UserMissionRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackPolicyValidator {
    private final FeedbackRepository feedbackRepository;
    private final UserMissionRepository userMissionRepository;

    public void validateNew(Long userId, Long userMissionId) {
        userMissionRepository.findByIdAndUserIdAndStateAndStatus(
                userMissionId,
                userId,
                UserMissionState.COMPLETED,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new ApiException(ErrorType.FEEDBACK_HAS_NOT_MISSION));

        feedbackRepository.findByUserIdAndUserMissionId(
                userId,
                userMissionId
        ).ifPresent(feedback -> {
            throw new ApiException(ErrorType.FEEDBACK_ALREADY_SUBMITTED);
        });
    }
}
