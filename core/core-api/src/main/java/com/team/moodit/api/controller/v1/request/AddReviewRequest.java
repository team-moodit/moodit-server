package com.team.moodit.api.controller.v1.request;

import com.team.moodit.domain.review.ReviewContent;
import com.team.moodit.domain.review.ReviewTarget;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.math.BigDecimal;

public record AddReviewRequest(
        Long userMissionId,
        BigDecimal rate,
        String content
) {
    public static final BigDecimal RATE_MIN = BigDecimal.valueOf(1); // 최소 별점
    public static final BigDecimal RATE_MAX = BigDecimal.valueOf(5); // 최대 별점
    public static final BigDecimal CONTENT_REQUIRED_RATE = BigDecimal.valueOf(3);

    public ReviewTarget toTarget() {
        if (userMissionId == null) throw new ApiException(ErrorType.INVALID_REQUEST);
        return new ReviewTarget(userMissionId);
    }

    public ReviewContent toContent() {
        if (rate == null || rate.compareTo(RATE_MIN) < 0) throw new ApiException(ErrorType.REVIEW_INVALID_RATE);
        if (rate.compareTo(RATE_MAX) > 0) throw new ApiException(ErrorType.REVIEW_INVALID_RATE);
        if (rate.remainder(BigDecimal.valueOf(0.5)).compareTo(BigDecimal.ZERO) != 0) throw new ApiException(ErrorType.REVIEW_INVALID_RATE);
        if (rate.compareTo(CONTENT_REQUIRED_RATE) < 0) {
            if (content == null || content.isBlank()) throw new ApiException(ErrorType.REVIEW_REQUIRED_CONTENT);
        }
        return new ReviewContent(rate, content);
    }
}
