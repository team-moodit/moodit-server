package com.team.moodit.api.assembler;

import com.team.moodit.api.controller.v1.UserMissionListType;
import com.team.moodit.api.controller.v1.response.UserMissionResponse;
import com.team.moodit.domain.feedback.FeedbackService;
import com.team.moodit.domain.match.MatchImage;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.domain.userMission.UserMission;
import com.team.moodit.domain.userMission.UserMissionService;
import com.team.moodit.support.OffsetLimit;
import com.team.moodit.support.Page;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.file.File;
import com.team.moodit.support.file.FileReader;
import com.team.moodit.support.response.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMissionAssembler {
    private final UserMissionService userMissionService;
    private final MatchService matchService;
    private final FileReader fileReader;
    private final FeedbackService feedbackService;

    public Page<UserMissionResponse> getUserMissions(ApiUser apiUser, UserMissionListType type, OffsetLimit offsetLimit) {
        Page<UserMission> missions = userMissionService.getUserMissions(apiUser, type, offsetLimit);
        List<MatchResult> matchResults = matchService.findMatchResults(missions.content().stream().map(UserMission::getMatchId).distinct().toList());
        List<MatchImage> matchImages = matchService.getMatchImages(matchResults.stream().map(MatchResult::getRepresentativeMatchImageId).toList());

        Map<Long, MatchResult> matchResultMap = matchResults.stream().collect(Collectors.toMap(MatchResult::getMatchId, it -> it));
        List<File> files = fileReader.getFiles(matchImages.stream().map(MatchImage::getFileId).toList());
        Map<Long, File> fileMap = files.stream().collect(Collectors.toMap(File::getId, it -> it));
        Map<Long, File> matchImageFileMap = matchImages.stream().collect(Collectors.toMap(
                MatchImage::getId,
                matchImage -> fileMap.get(matchImage.getFileId())
        ));

        Map<Long, Double> scoreMap = feedbackService.scores(missions.content().stream().map(UserMission::getId).toList());

        return new Page<>(
                UserMissionResponse.of(
                        missions.content(),
                        matchResultMap,
                        matchImageFileMap,
                        scoreMap
                ),
                missions.totalCount(),
                missions.hasNext()
        );
    }

    public UserMissionResponse getUserMission(ApiUser apiUser, Long userMissionId) {
        UserMission userMission = userMissionService.getUserMission(apiUser, userMissionId);
        MatchResult matchResult = matchService.getMatchResult(apiUser, userMission.getMatchId());
        MatchImage matchImage = matchService.getMatchImage(matchResult.getRepresentativeMatchImageId());
        Map<Long, Double> scoreMap = feedbackService.scores(List.of(userMissionId));
        File file = fileReader.getFile(matchImage.getFileId());
        return UserMissionResponse.of(userMission, matchResult, file, scoreMap.get(userMissionId));
    }
}
