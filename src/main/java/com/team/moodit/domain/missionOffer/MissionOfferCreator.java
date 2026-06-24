package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.enums.MissionOfferState;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.mission.MissionTemplate;
import com.team.moodit.storage.db.core.MissionOfferCandidateEntity;
import com.team.moodit.storage.db.core.MissionOfferCandidateRepository;
import com.team.moodit.storage.db.core.MissionOfferEntity;
import com.team.moodit.storage.db.core.MissionOfferRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionOfferCreator {
    private final MissionOfferRepository missionOfferRepository;
    private final MissionOfferCandidateRepository missionOfferCandidateRepository;

    @Transactional
    public MissionOfferCreateResult createSelectionOffer(Long userId, MatchResult matchResult, List<MissionTemplate> missionTemplates) {
        MissionOffer missionOffer = createOffer(
                userId,
                matchResult,
                missionTemplates,
                MissionOfferState.NEEDS_SELECTION
        );
        return MissionOfferCreateResult.needsSelection(missionOffer);
    }

    @Transactional
    public MissionOffer createReadyToAcceptOffer(Long userId, MatchResult matchResult, MissionTemplate missionTemplate) {
        return createOffer(
                userId,
                matchResult,
                List.of(missionTemplate),
                MissionOfferState.READY_TO_ACCEPT
        );
    }

    private MissionOffer createOffer(
            Long userId,
            MatchResult matchResult,
            List<MissionTemplate> missionTemplates,
            MissionOfferState state
    ) {
        boolean isExists = missionOfferRepository.existsByMatchIdAndUserId(matchResult.getMatchId(), userId);
        if (isExists) throw new ApiException(ErrorType.ALREADY_PROCESSED);

        MissionOfferEntity offer = missionOfferRepository.save(
                new MissionOfferEntity(matchResult.getMatchId(), userId, state)
        );

        List<MissionOfferCandidateEntity> candidates = missionTemplates.stream().map(it ->
                new MissionOfferCandidateEntity(
                        offer.getId(),
                        it.getId(),
                        it.getTitle(),
                        it.getDisplayOrder()
                )
        ).toList();
        missionOfferCandidateRepository.saveAll(candidates);

        return new MissionOffer(
                offer.getId(),
                offer.getMatchId(),
                offer.getUserId(),
                candidates.stream().map(it ->
                        new MissionCandidate(
                                it.getId(),
                                it.getOfferId(),
                                it.getMissionTemplateId(),
                                it.getTitle(),
                                it.getDisplayOrder()
                        )
                ).toList(),
                offer.getState()
        );
    }
}
