package com.team.moodit.domain.review;

import com.team.moodit.storage.db.core.ReviewEntity;
import com.team.moodit.storage.db.core.ReviewRepository;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewPolicyValidator {
    private final ReviewRepository reviewRepository;

    public void validateNew(ApiUser apiUser, ReviewTarget target) {
        Optional<ReviewEntity> reviewOpt = reviewRepository.findByUserIdAndUserMissionId(apiUser.getId(), target.getUserMissionId());
        if (reviewOpt.isPresent()) throw new ApiException(ErrorType.REVIEW_ALREADY_SUBMITTED);
    }
}
