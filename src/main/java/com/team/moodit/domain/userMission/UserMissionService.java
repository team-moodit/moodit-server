package com.team.moodit.domain.userMission;

import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMissionService {
    private final UserMissionReader userMissionReader;

    public UserMission getUserMission(ApiUser apiUser, Long userMissionId) {
        return userMissionReader.getUserMission(apiUser.getId(), userMissionId);
    }
}
