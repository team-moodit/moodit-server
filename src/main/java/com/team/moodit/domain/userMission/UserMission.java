package com.team.moodit.domain.userMission;

import com.team.moodit.domain.enums.UserMissionState;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMission {
    private Long id;
    private Long matchId;
    private String title;
    private UserMissionState state;
    private LocalDateTime completedAt;
}
