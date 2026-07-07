package com.team.moodit.domain.userMission;

import com.team.moodit.domain.enums.EntityStatus;
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

    public Page<UserMission> getUserMissions(Long userId, UserMissionState state, OffsetLimit offsetLimit) {
        org.springframework.data.domain.Page<UserMissionEntity> missions = userMissionRepository.findByUserIdAndStateAndStatusOrderByIdDesc(
                userId,
                state,
                EntityStatus.ACTIVE,
                offsetLimit.toPageable()
        );

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
        UserMissionEntity entity = userMissionRepository.findByIdAndUserIdAndStatus(userMissionId, userId, EntityStatus.ACTIVE)
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
