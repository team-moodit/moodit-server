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
    public MissionOfferCreateResult createAcceptedOfferAndMission(Long userId, MatchResult matchResult, MissionTemplate missionTemplate) {
        MissionOffer missionOffer = missionOfferCreator.createReadyToAcceptOffer(
                userId,
                matchResult,
                missionTemplate
        );

        MissionCandidate candidate = missionOffer.getOnlyCandidate();
        missionOfferManager.accept(missionOffer.getId(), candidate.getId());

        Long userMissionId = userMissionManager.create(
                userId,
                matchResult.getMatchId(),
                missionOffer.getId(),
                candidate.getMissionTemplateId(),
                candidate.getTitle()
        );

        MissionOffer savedOffer = missionOfferReader.getMissionOffer(missionOffer.getId());
        return MissionOfferCreateResult.accepted(savedOffer, userMissionId);
    }

    @Transactional
    public Long accept(OfferAcceptAction action) {
        MissionOffer missionOffer = missionOfferReader.getMissionOffer(action.getOfferId());
        MissionCandidate candidate = missionOffer.getCandidate(action.getCandidateId());

        missionOfferManager.accept(action.getOfferId(), action.getCandidateId());

        return userMissionManager.create(
                missionOffer.getUserId(),
                missionOffer.getMatchId(),
                missionOffer.getId(),
                candidate.getMissionTemplateId(),
                candidate.getTitle()
        );
    }
}
