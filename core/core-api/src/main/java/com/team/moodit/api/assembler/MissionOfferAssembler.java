package com.team.moodit.api.assembler;

import com.team.moodit.api.controller.v1.request.CreateMissionOfferRequest;
import com.team.moodit.api.controller.v1.response.MissionOfferResponse;
import com.team.moodit.domain.match.MatchImage;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.match.MatchResultService;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.domain.missionOffer.MissionOfferCreateResult;
import com.team.moodit.domain.missionOffer.MissionOfferService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.file.File;
import com.team.moodit.support.file.FileReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionOfferAssembler {
    private final MissionOfferService missionOfferService;
    private final MatchService matchService;
    private final MatchResultService matchResultService;
    private final FileReader fileReader;

    public MissionOfferResponse getMissionOffer(ApiUser apiUser, CreateMissionOfferRequest request) {
        MissionOfferCreateResult createResult = missionOfferService.getOrCreateOffer(apiUser, request.matchResultId());
        MatchResult matchResult = matchResultService.getMatchResult(apiUser, request.matchResultId());
        MatchImage matchImage = matchService.getMatchImage(matchResult.getRepresentativeMatchImageId());
        File representativeImageFile = fileReader.getFile(matchImage.getFileId());
        return MissionOfferResponse.of(createResult, matchResult, representativeImageFile);
    }
}
