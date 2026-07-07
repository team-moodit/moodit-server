package com.team.moodit.domain.review;

import com.team.moodit.storage.db.core.ReviewEntity;
import com.team.moodit.storage.db.core.ReviewRepository;
import com.team.moodit.storage.db.core.UserMissionEntity;
import com.team.moodit.storage.db.core.UserMissionRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReviewManager {
    private final ReviewRepository reviewRepository;
    private final UserMissionRepository userMissionRepository;

    @Transactional
    public Long add(Long userId, ReviewTarget target, ReviewContent content) {
        UserMissionEntity userMission = userMissionRepository.findById(target.getUserMissionId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        userMission.reviewed();

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
