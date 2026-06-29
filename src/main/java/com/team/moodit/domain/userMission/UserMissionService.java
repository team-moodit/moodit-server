package com.team.moodit.domain.userMission;

import com.team.moodit.api.controller.v1.UserMissionListType;
import com.team.moodit.support.OffsetLimit;
import com.team.moodit.support.Page;
import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMissionService {
    private final UserMissionReader userMissionReader;
    private final UserMissionManager userMissionManager;

    public Page<UserMission> getUserMissions(ApiUser apiUser, UserMissionListType type, OffsetLimit offsetLimit) {
        return userMissionReader.getUserMissions(apiUser.getId(), type, offsetLimit);
    }

    public UserMission getUserMission(ApiUser apiUser, Long userMissionId) {
        return userMissionReader.getUserMission(apiUser.getId(), userMissionId);
    }

    public Long completeUserMission(ApiUser apiUser, Long userMissionId) {
        return userMissionManager.complete(apiUser.getId(), userMissionId);
    }
}
