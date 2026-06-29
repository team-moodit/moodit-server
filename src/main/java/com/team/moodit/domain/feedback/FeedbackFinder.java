package com.team.moodit.domain.feedback;

import com.team.moodit.storage.db.core.BaseIdEntity;
import com.team.moodit.storage.db.core.FeedbackEntity;
import com.team.moodit.storage.db.core.FeedbackRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackFinder {
    private final FeedbackRepository feedbackRepository;

    public Map<Long, Double> scoreByMissionIds(List<Long> missionIds) {
        if (missionIds.isEmpty()) {
            return Map.of();
        }

        List<FeedbackEntity> results = feedbackRepository.findByUserMissionIdIn(
                missionIds
        );

        return results.stream().collect(Collectors.toMap(
                FeedbackEntity::getUserMissionId,
                FeedbackEntity::getSatisfactionScore
        ));
    }
}
