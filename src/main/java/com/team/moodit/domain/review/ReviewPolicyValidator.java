package com.team.moodit.domain.review;

import com.team.moodit.storage.db.core.ReviewRepository;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewPolicyValidator {
    private final ReviewRepository reviewRepository;

    public void validateNew(ApiUser apiUser, ReviewTarget target) {
        reviewRepository.findByUserIdAndUserMissionId(apiUser.getId(), target.getUserMissionId())
                .orElseThrow(() -> new ApiException(ErrorType.REVIEW_HAS_NOT_MISSION));
    }
}
