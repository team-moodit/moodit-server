package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.review.Review;
import com.team.moodit.domain.userMission.UserMission;
import com.team.moodit.support.file.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record UserMissionResponse(
        Long userMissionId,
        String missionTitle,
        UserMissionState missionState,
        LocalDateTime missionCompletedAt,
        MatchResultResponse matchResult,
        BigDecimal rate

) {
    public static List<UserMissionResponse> of(
            List<UserMission> missions,
            Map<Long, MatchResult> matchResultMap,
            Map<Long, File> matchRepresentativeImageFileMap,
            Map<Long, Review> reviewMap
    ) {
        return missions.stream().map(it ->
                of(
                        it,
                        matchResultMap.get(it.getMatchId()),
                        matchRepresentativeImageFileMap.get(matchResultMap.get(it.getMatchId()).getRepresentativeMatchImageId()),
                        reviewMap.get(it.getId())
                )
        ).toList();
    }

    public static UserMissionResponse of(
            UserMission missions,
            MatchResult matchResult,
            File matchRepresentativeImageFile,
            Review review
    ) {
        return new UserMissionResponse(
                missions.getId(),
                missions.getTitle(),
                missions.getState(),
                missions.getCompletedAt(),
                MatchResultResponse.of(matchResult, matchRepresentativeImageFile),
                review != null ? review.getContent().getRate() : null
        );
    }
}
