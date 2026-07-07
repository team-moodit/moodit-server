package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.storage.db.core.MissionOfferCandidateEntity;
import com.team.moodit.storage.db.core.MissionOfferCandidateRepository;
import com.team.moodit.storage.db.core.MissionOfferEntity;
import com.team.moodit.storage.db.core.MissionOfferRepository;
import com.team.moodit.storage.db.core.UserMissionEntity;
import com.team.moodit.storage.db.core.UserMissionRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionOfferReader {
    private final MissionOfferRepository missionOfferRepository;
    private final MissionOfferCandidateRepository missionOfferCandidateRepository;
    private final UserMissionRepository userMissionRepository;

    public Optional<MissionOfferCreateResult> findCreateResultByUserIdAndMatchResultId(
            Long userId,
            Long matchResultId,
            PreferenceResultType preferenceResultType
    ) {
        return missionOfferRepository.findByUserIdAndMatchResultId(userId, matchResultId)
                .map(offerEntity -> {
                    MissionOffer offer = toMissionOffer(offerEntity);

                    Long assignedMissionId = userMissionRepository
                            .findByMissionOfferIdAndStatus(offer.getId(), EntityStatus.ACTIVE)
                            .map(UserMissionEntity::getId)
                            .orElse(null);

                    return MissionOfferCreateResult.of(offer, preferenceResultType, assignedMissionId);
                });
    }

    private MissionOffer toMissionOffer(MissionOfferEntity offer) {
        List<MissionCandidate> candidates = missionOfferCandidateRepository.findByOfferId(offer.getId()).stream()
                .map(it ->
                        new MissionCandidate(
                                it.getId(),
                                it.getOfferId(),
                                it.getMissionTemplateId(),
                                it.getTitle(),
                                it.getDisplayOrder()
                        )
                ).toList();

        return new MissionOffer(
                offer.getId(),
                offer.getMatchResultId(),
                offer.getUserId(),
                candidates,
                offer.getState()
        );
    }

    public MissionOffer getMissionOffer(Long missionOfferId) {
        MissionOfferEntity offerEntity = missionOfferRepository.findById(missionOfferId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));
        List<MissionOfferCandidateEntity> candidateEntities = missionOfferCandidateRepository.findByOfferId(offerEntity.getId());

        return new MissionOffer(
                offerEntity.getId(),
                offerEntity.getMatchResultId(),
                offerEntity.getUserId(),
                candidateEntities.stream().map(it ->
                        new MissionCandidate(
                                it.getId(),
                                it.getOfferId(),
                                it.getMissionTemplateId(),
                                it.getTitle(),
                                it.getDisplayOrder()
                        )
                ).toList(),
                offerEntity.getState()
        );
    }
}
