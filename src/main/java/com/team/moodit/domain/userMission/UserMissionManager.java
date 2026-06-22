package com.team.moodit.domain.userMission;

import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.storage.db.core.UserMissionEntity;
import com.team.moodit.storage.db.core.UserMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
