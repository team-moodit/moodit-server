package com.team.moodit.domain.userMission;

import com.team.moodit.storage.db.core.UserMissionEntity;
import com.team.moodit.storage.db.core.UserMissionRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMissionReader {
    private final UserMissionRepository userMissionRepository;

    public UserMission getUserMission(Long userId, Long userMissionId) {
        UserMissionEntity entity = userMissionRepository.findByIdAndUserId(userMissionId, userId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        return new UserMission(
                entity.getId(),
                entity.getMatchId(),
                entity.getTitle(),
                entity.getState()
        );
    }
}
