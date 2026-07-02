package com.team.moodit.domain.review;

import com.team.moodit.support.auth.ApiUser;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewFinder reviewFinder;
    private final ReviewManager reviewManager;
    private final ReviewPolicyValidator reviewPolicyValidator;

    public RateSummary findRateSummary(ApiUser apiUser) {
        return reviewFinder.findRateSummary(apiUser.getId());
    }

    public List<Review> findReviews(List<ReviewTarget> targets) {
        return reviewFinder.find(targets);
    }

    public Review findReview(ReviewTarget target) {
        return reviewFinder.find(target);
    }

    public Long addReview(ApiUser apiUser, ReviewTarget target, ReviewContent content) {
        reviewPolicyValidator.validateNew(apiUser, target);
        return reviewManager.add(apiUser.getId(), target, content);
    }
}
