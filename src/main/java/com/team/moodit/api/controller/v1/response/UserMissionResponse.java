package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.PreferenceType;
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
        LocalDateTime missionCompletedAt,
        String matchTitle,
        String matchRepresentativeImageUrl, // 매치 대표 이미지
        String matchPreferenceType,
        int matchRoundCount,
        LocalDateTime matchCompletedAt,
        Double satisfactionScore

) {
    public static List<UserMissionResponse> of(
            List<UserMission> missions,
            Map<Long, MatchResult> matchResultMap,
            Map<Long, File> matchRepresentativeImageFileMap,
            Map<Long, Double> satisfactionScoreMap
    ) {
        return missions.stream().map(it ->
                of(
                        it,
                        matchResultMap.get(it.getMatchId()),
                        matchRepresentativeImageFileMap.get(matchResultMap.get(it.getMatchId()).getRepresentativeMatchImageId()),
                        satisfactionScoreMap.get(it.getId())
                )
        ).toList();
    }

    public static UserMissionResponse of(
            UserMission missions,
            MatchResult matchResult,
            File matchRepresentativeImageFile,
            Double satisfactionScore
    ) {
        PreferenceType preferenceType = matchResult.getPreferenceResult().getPreferenceType();

        return new UserMissionResponse(
                missions.getId(),
                missions.getTitle(),
                missions.getState(),
                missions.getCompletedAt(),
                matchResult.getTitle(),
                matchRepresentativeImageFile.getUrl(),
                preferenceType != null ? preferenceType.getTitle() : null,
                matchResult.getRoundCount(),
                matchResult.getCompletedAt(),
                satisfactionScore
        );
    }
}
