package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.enums.MissionOfferState;
import com.team.moodit.storage.db.core.MissionOfferEntity;
import com.team.moodit.storage.db.core.MissionOfferRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionOfferAcceptor {
    private final MissionOfferRepository missionOfferRepository;

    @Transactional
    public void accept(Long offerId, Long candidateId) {
        MissionOfferEntity offer = missionOfferRepository.findByIdAndState(offerId, MissionOfferState.NEEDS_SELECTION)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        offer.accepted(candidateId);
    }
}
