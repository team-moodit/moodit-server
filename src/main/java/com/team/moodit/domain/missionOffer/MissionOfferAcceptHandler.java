package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.match.MatchResultFinder;
import com.team.moodit.domain.mission.MissionTemplate;
import com.team.moodit.domain.userMission.UserMissionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionOfferAcceptHandler {
    private final MissionOfferCreator missionOfferCreator;
    private final MissionOfferAcceptor missionOfferAcceptor;
    private final UserMissionManager userMissionManager;
    private final MissionOfferReader missionOfferReader;
    private final MatchResultFinder matchResultFinder;

    @Transactional
    public MissionOfferCreateResult createAcceptedOfferAndMission(Long userId, MatchResult matchResult, MissionTemplate missionTemplate) {
        MissionOffer missionOffer = missionOfferCreator.createSingleCandidateOffer(
                userId,
                matchResult,
                missionTemplate
        );

        MissionCandidate candidate = missionOffer.getOnlyCandidate();
        missionOfferAcceptor.accept(missionOffer.getId(), candidate.getId());

        Long userMissionId = userMissionManager.create(
                userId,
                matchResult.getMatchId(),
                missionOffer.getId(),
                candidate.getMissionTemplateId(),
                candidate.getTitle()
        );

        MissionOffer savedOffer = missionOfferReader.getMissionOffer(missionOffer.getId());
        return MissionOfferCreateResult.assigned(
                savedOffer,
                matchResult.getPreferenceResult().getResultType(),
                userMissionId
        );
    }

    @Transactional
    public Long accept(OfferAcceptAction action) {
        MissionOffer missionOffer = missionOfferReader.getMissionOffer(action.getOfferId());
        MissionCandidate candidate = missionOffer.getCandidate(action.getCandidateId());
        Long matchId = matchResultFinder.findOwnedMatchId(missionOffer.getUserId(), missionOffer.getMatchResultId());

        missionOfferAcceptor.accept(action.getOfferId(), action.getCandidateId());

        return userMissionManager.create(
                missionOffer.getUserId(),
                matchId,
                missionOffer.getId(),
                candidate.getMissionTemplateId(),
                candidate.getTitle()
        );
    }
}
