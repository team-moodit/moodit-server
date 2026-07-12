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
    public MissionOffer createOffer(Long userId, MatchResult matchResult, List<MissionTemplate> missionTemplates) {
        if (missionTemplates.isEmpty()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        MissionOfferEntity offer = missionOfferRepository.save(
                new MissionOfferEntity(
                        matchResult.getId(),
                        userId,
                        MissionOfferState.NEEDS_SELECTION
                )
        );

        List<MissionOfferCandidateEntity> savedCandidates = missionOfferCandidateRepository.saveAll(
                missionTemplates.stream().map(it ->
                        new MissionOfferCandidateEntity(
                                offer.getId(),
                                it.getId(),
                                it.getTitle(),
                                it.getDisplayOrder()
                        )
                ).toList()
        );

        return new MissionOffer(
                offer.getId(),
                offer.getMatchResultId(),
                offer.getUserId(),
                savedCandidates.stream()
                        .map(it -> new MissionCandidate(
                                it.getId(),
                                it.getOfferId(),
                                it.getMissionTemplateId(),
                                it.getTitle(),
                                it.getDisplayOrder()
                        ))
                        .toList(),
                offer.getState()
        );
    }
}
