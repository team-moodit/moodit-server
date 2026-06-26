package com.team.moodit.domain.userMission;

import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.support.OffsetLimit;
import com.team.moodit.support.Page;
import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMissionService {
    private final UserMissionReader userMissionReader;

    public Page<UserMission> getUserMissions(ApiUser apiUser, UserMissionState state, OffsetLimit offsetLimit) {
        return userMissionReader.getUserMissions(apiUser.getId(), state, offsetLimit);
    }

    public UserMission getUserMission(ApiUser apiUser, Long userMissionId) {
        return userMissionReader.getUserMission(apiUser.getId(), userMissionId);
    }
}
