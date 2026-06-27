package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.userMission.UserMission;
import com.team.moodit.support.file.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record UserMissionResponse(
        Long userMissionId,
        String missionTitle,
        UserMissionState missionState,
        String matchTitle,
        String matchRepresentativeImageUrl, // 매치 대표 이미지
        int roundCount,
        LocalDateTime matchCompletedAt
) {
    public static List<UserMissionResponse> of(
            List<UserMission> missions,
            Map<Long, MatchResult> matchResultMap,
            Map<Long, File> matchRepresentativeImageFileMap
    ) {
        return missions.stream().map(it ->
                of(
                        it,
                        matchResultMap.get(it.getMatchId()),
                        matchRepresentativeImageFileMap.get(matchResultMap.get(it.getMatchId()).getRepresentativeMatchImageId())
                )
        ).toList();
    }

    public static UserMissionResponse of(
            UserMission missions,
            MatchResult matchResult,
            File matchRepresentativeImageFile
    ) {
        return new UserMissionResponse(
                missions.getId(),
                missions.getTitle(),
                missions.getState(),
                matchResult.getTitle(),
                matchRepresentativeImageFile.getUrl(),
                matchResult.getRoundCount(),
                matchResult.getCompletedAt()
        );
    }
}
