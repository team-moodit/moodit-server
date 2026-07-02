package com.team.moodit.domain.review;

import com.team.moodit.storage.db.core.ReviewEntity;
import com.team.moodit.storage.db.core.ReviewRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewFinder {
    private final ReviewRepository reviewRepository;

    /**
     * 유저의 유저 미션 리뷰 전체 기준
     */
    public RateSummary findRateSummary(Long userId) {
        List<ReviewEntity> found = reviewRepository.findByUserId(userId);
        if (found.isEmpty()) {
            return RateSummary.EMPTY;
        } else {
            BigDecimal rateSum = found.stream().map(ReviewEntity::getRate).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new RateSummary(
                    rateSum.divide(BigDecimal.valueOf(found.size()), 1, RoundingMode.HALF_UP),
                    found.size()
            );
        }
    }

    public List<Review> find(List<ReviewTarget> targets) {
        List<ReviewEntity> found = reviewRepository.findByUserMissionIdIn(targets.stream().map(ReviewTarget::getUserMissionId).toList());
        return found.stream().map(it ->
                new Review(
                        it.getId(),
                        it.getUserId(),
                        new ReviewTarget(
                                it.getUserMissionId()
                        ),
                        new ReviewContent(
                                it.getRate(),
                                it.getContent()
                        )
                )
        ).toList();
    }

    public Review find(ReviewTarget target) {
        ReviewEntity found = reviewRepository.findByUserMissionId(target.getUserMissionId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));
        return new Review(
                found.getId(),
                found.getUserId(),
                new ReviewTarget(
                        found.getUserMissionId()
                ),
                new ReviewContent(
                        found.getRate(),
                        found.getContent()
                )
        );
    }
}
