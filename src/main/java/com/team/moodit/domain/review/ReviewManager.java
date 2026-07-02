package com.team.moodit.domain.review;

import com.team.moodit.storage.db.core.ReviewEntity;
import com.team.moodit.storage.db.core.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReviewManager {
    private final ReviewRepository reviewRepository;

    @Transactional
    public Long add(Long userId, ReviewTarget target, ReviewContent content) {
        ReviewEntity saved = reviewRepository.save(
                new ReviewEntity(
                        userId,
                        target.getUserMissionId(),
                        content.getRate(),
                        content.getContent()
                )
        );

        return saved.getId();
    }
}
