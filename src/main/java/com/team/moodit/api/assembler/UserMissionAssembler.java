package com.team.moodit.api.assembler;

import com.team.moodit.api.controller.v1.response.UserMissionResponse;
import com.team.moodit.domain.match.MatchImage;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.domain.userMission.UserMission;
import com.team.moodit.domain.userMission.UserMissionService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.file.File;
import com.team.moodit.support.file.FileReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMissionAssembler {
    private final UserMissionService userMissionService;
    private final MatchService matchService;
    private final FileReader fileReader;

    public UserMissionResponse getUserMission(ApiUser apiUser, Long userMissionId) {
        UserMission userMission = userMissionService.getUserMission(apiUser, userMissionId);
        MatchResult matchResult = matchService.getMatchResult(apiUser, userMission.getMatchId());
        MatchImage matchImage = matchService.getMatchImage(matchResult.getRepresentativeMatchImageId());
        File file = fileReader.getFile(matchImage.getFileId());
        return UserMissionResponse.of(userMission, matchResult, file);
    }
}
