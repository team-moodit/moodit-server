package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.enums.MissionOfferState;
import com.team.moodit.storage.db.core.MissionOfferCandidateEntity;
import com.team.moodit.storage.db.core.MissionOfferCandidateRepository;
import com.team.moodit.storage.db.core.MissionOfferEntity;
import com.team.moodit.storage.db.core.MissionOfferRepository;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionOfferAcceptValidator {
    private final MissionOfferRepository missionOfferRepository;
    private final MissionOfferCandidateRepository missionOfferCandidateRepository;

    public void validate(ApiUser apiUser, OfferAcceptAction action) {
        MissionOfferEntity offer = missionOfferRepository.findByIdAndState(action.getOfferId(), MissionOfferState.NEEDS_SELECTION)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (!Objects.equals(offer.getUserId(), apiUser.getId())) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        List<MissionOfferCandidateEntity> candidates = missionOfferCandidateRepository.findByOfferId(offer.getId());
        if (candidates.isEmpty()) throw new ApiException(ErrorType.NOT_FOUND);
        boolean hasCandidate = candidates.stream()
                .anyMatch(it -> Objects.equals(it.getId(), action.getCandidateId()));
        if (!hasCandidate) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }
    }
}
