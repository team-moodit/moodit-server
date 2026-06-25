package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.userMission.UserMission;
import com.team.moodit.support.file.File;
import java.time.LocalDateTime;

public record UserMissionResponse(
        Long userMissionId,
        String missionTitle,
        UserMissionState missionState,
        String matchTitle,
        String matchRepresentativeImageUrl, // 매치 대표 이미지
        int roundCount,
        LocalDateTime matchCompletedAt
) {
    public static UserMissionResponse of(
            UserMission userMission,
            MatchResult matchResult,
            File matchRepresentativeImageFile
    ) {
        return new UserMissionResponse(
                userMission.getId(),
                userMission.getTitle(),
                userMission.getState(),
                matchResult.getTitle(),
                matchRepresentativeImageFile.getUrl(),
                matchResult.getRoundCount(),
                matchResult.getCompletedAt()
        );
    }
}
