package com.team.moodit.domain.userMission;

import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.storage.db.core.UserMissionEntity;
import com.team.moodit.storage.db.core.UserMissionRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserMissionManager {
    private final UserMissionRepository userMissionRepository;

    public Long create(Long userId, Long matchId, Long missionOfferId, Long missionTemplateId, String title) {
        return userMissionRepository.save(
                new UserMissionEntity(
                        userId,
                        matchId,
                        missionOfferId,
                        missionTemplateId,
                        title,
                        UserMissionState.IN_PROGRESS
                )
        ).getId();
    }

    @Transactional
    public Long complete(Long userId, Long userMissionId) {
        UserMissionEntity userMission = userMissionRepository.findByIdAndUserId(userMissionId, userId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));
        if (userMission.getState() != UserMissionState.IN_PROGRESS) {
            throw new ApiException(ErrorType.USER_MISSION_INVALID_STATE);
        }

        userMission.completed();

        return userMissionId;
    }
}
