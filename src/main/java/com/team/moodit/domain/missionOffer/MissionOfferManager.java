package com.team.moodit.domain.missionOffer;

import com.team.moodit.storage.db.core.MissionOfferEntity;
import com.team.moodit.storage.db.core.MissionOfferRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionOfferManager {
    private final MissionOfferRepository missionOfferRepository;

    @Transactional
    public void accept(Long offerId, Long candidateId) {
        MissionOfferEntity offer = missionOfferRepository.findById(offerId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        offer.accepted(candidateId);
    }
}
