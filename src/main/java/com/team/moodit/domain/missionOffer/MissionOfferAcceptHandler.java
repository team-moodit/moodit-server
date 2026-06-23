package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.mission.MissionTemplate;
import com.team.moodit.domain.userMission.UserMissionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionOfferAcceptHandler {
    private final MissionOfferCreator missionOfferCreator;
    private final MissionOfferManager missionOfferManager;
    private final UserMissionManager userMissionManager;
    private final MissionOfferReader missionOfferReader;

    @Transactional
    public MissionOffer createAcceptedOfferAndMission(Long userId, MatchResult matchResult, MissionTemplate missionTemplate) {
        MissionOffer missionOffer = missionOfferCreator.createReadyToAcceptOffer(
                userId,
                matchResult,
                missionTemplate
        );

        MissionCandidate candidate = missionOffer.getOnlyCandidate();
        missionOfferManager.accept(missionOffer.getId(), candidate.getId());

        userMissionManager.create(userId, matchResult.getMatchId(), missionOffer.getId(), candidate.getMissionTemplateId(), candidate.getTitle());

        return missionOfferReader.getMissionOffer(missionOffer.getId());
    }
}
