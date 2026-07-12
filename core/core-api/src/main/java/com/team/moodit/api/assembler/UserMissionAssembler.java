package com.team.moodit.api.assembler;

import com.team.moodit.api.controller.v1.response.UserMissionResponse;
import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.domain.match.MatchImage;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.domain.review.Review;
import com.team.moodit.domain.review.ReviewFinder;
import com.team.moodit.domain.review.ReviewService;
import com.team.moodit.domain.review.ReviewTarget;
import com.team.moodit.domain.userMission.UserMission;
import com.team.moodit.domain.userMission.UserMissionService;
import com.team.moodit.support.OffsetLimit;
import com.team.moodit.support.Page;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.file.File;
import com.team.moodit.support.file.FileReader;
import java.math.BigDecimal;
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
    private final ReviewService reviewService;

    public Page<UserMissionResponse> getUserMissions(ApiUser apiUser, UserMissionState state, OffsetLimit offsetLimit) {
        Page<UserMission> missions = userMissionService.getUserMissions(apiUser, state, offsetLimit);
        List<MatchResult> matchResults = matchService.findMatchResults(missions.content().stream().map(UserMission::getMatchId).distinct().toList());
        List<MatchImage> matchImages = matchService.getMatchImages(matchResults.stream().map(MatchResult::getRepresentativeMatchImageId).toList());

        Map<Long, MatchResult> matchResultMap = matchResults.stream().collect(Collectors.toMap(MatchResult::getMatchId, it -> it));
        List<File> files = fileReader.getFiles(matchImages.stream().map(MatchImage::getFileId).toList());
        Map<Long, File> fileMap = files.stream().collect(Collectors.toMap(File::getId, it -> it));
        Map<Long, File> matchImageFileMap = matchImages.stream().collect(Collectors.toMap(
                MatchImage::getId,
                matchImage -> fileMap.get(matchImage.getFileId())
        ));

        List<ReviewTarget> reviewTargets = missions.content().stream().map(it -> new ReviewTarget(it.getId())).toList();
        Map<Long, Review> reviewMap = reviewService.findReviews(reviewTargets).stream().collect(Collectors.toMap(
                it -> it.getTarget().getUserMissionId(),
                it -> it
        ));

        return new Page<>(
                UserMissionResponse.of(
                        missions.content(),
                        matchResultMap,
                        matchImageFileMap,
                        reviewMap
                ),
                missions.totalCount(),
                missions.hasNext()
        );
    }

    public UserMissionResponse getUserMission(ApiUser apiUser, Long userMissionId) {
        UserMission userMission = userMissionService.getUserMission(apiUser, userMissionId);
        MatchResult matchResult = matchService.findMatchResult(apiUser, userMission.getMatchId());
        MatchImage matchImage = matchService.getMatchImage(matchResult.getRepresentativeMatchImageId());
        Review review = reviewService.findReviewOrNull(new ReviewTarget(userMission.getId()));
        File file = fileReader.getFile(matchImage.getFileId());
        return new UserMissionResponse(userMission, matchResult, file, review);
    }
}
