package com.team.moodit.domain.review;

import com.team.moodit.storage.db.core.ReviewEntity;
import com.team.moodit.storage.db.core.ReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
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

    public Review findOrNull(ReviewTarget target) {
        Optional<ReviewEntity> foundOpt = reviewRepository.findByUserMissionId(target.getUserMissionId());

        if (foundOpt.isPresent()) {
            ReviewEntity review = foundOpt.get();
            return new Review(
                    review.getId(),
                    review.getUserId(),
                    new ReviewTarget(
                            review.getUserMissionId()
                    ),
                    new ReviewContent(
                            review.getRate(),
                            review.getContent()
                    )
            );
        } else {
            return null;
        }
    }
}
