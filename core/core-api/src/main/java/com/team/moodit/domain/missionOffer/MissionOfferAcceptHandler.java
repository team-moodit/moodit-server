package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.match.MatchResultFinder;
import com.team.moodit.domain.mission.MissionTemplate;
import com.team.moodit.domain.userMission.UserMissionManager;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
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
    private final MatchRepository matchRepository;

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

        // 임시 코드 개념리팩토링
        MatchEntity matchEntity = matchRepository.findByIdAndUserId(matchResult.getMatchId(), userId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));
        matchEntity.complete();

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

        // 임시 코드 개념리팩토링
        MatchEntity matchEntity = matchRepository.findByIdAndUserId(matchId, missionOffer.getUserId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));
        matchEntity.complete();

        return userMissionManager.create(
                missionOffer.getUserId(),
                matchId,
                missionOffer.getId(),
                candidate.getMissionTemplateId(),
                candidate.getTitle()
        );
    }
}
