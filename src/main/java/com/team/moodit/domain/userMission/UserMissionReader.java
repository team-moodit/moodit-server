package com.team.moodit.domain.userMission;

import com.team.moodit.api.controller.v1.UserMissionListType;
import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.storage.db.core.UserMissionEntity;
import com.team.moodit.storage.db.core.UserMissionRepository;
import com.team.moodit.support.OffsetLimit;
import com.team.moodit.support.Page;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMissionReader {
    private final UserMissionRepository userMissionRepository;

    public Page<UserMission> getUserMissions(Long userId, UserMissionListType type, OffsetLimit offsetLimit) {
        org.springframework.data.domain.Page<UserMissionEntity> missions = switch (type) {
            case IN_PROGRESS -> userMissionRepository.findByUserIdAndStateOrderByIdDesc(
                    userId,
                    UserMissionState.IN_PROGRESS,
                    offsetLimit.toPageable()
            );
            case COMPLETED -> userMissionRepository.findByUserIdAndStateOrderByIdDesc(
                    userId,
                    UserMissionState.COMPLETED,
                    offsetLimit.toPageable()
            );
            case FEEDBACK_SUBMITTED -> userMissionRepository.findCompletedWithFeedback(
                    userId,
                    offsetLimit.toPageable()
            );
        };

        return new Page<>(
                missions.getContent().stream().map(it ->
                        new UserMission(
                                it.getId(),
                                it.getMatchId(),
                                it.getTitle(),
                                it.getState(),
                                it.getCompletedAt()
                        )
                ).toList(),
                missions.getTotalElements(),
                missions.hasNext()
        );
    }

    public UserMission getUserMission(Long userId, Long userMissionId) {
        UserMissionEntity entity = userMissionRepository.findByIdAndUserId(userMissionId, userId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        return new UserMission(
                entity.getId(),
                entity.getMatchId(),
                entity.getTitle(),
                entity.getState(),
                entity.getCompletedAt()
        );
    }
}
