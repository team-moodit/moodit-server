package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.match.MatchResultFinder;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionOfferService {
    private final MissionOfferAcceptHandler missionOfferAcceptHandler;
    private final MissionOfferAcceptValidator missionOfferAcceptValidator;
    private final MissionOfferManager missionOfferManager;
    private final MatchResultFinder matchResultFinder;

    public MissionOfferCreateResult getOrCreateOffer(ApiUser apiUser, Long matchResultId) {
        MatchResult matchResult = matchResultFinder.findOwnedByUser(apiUser.getId(), matchResultId);
        return missionOfferManager.getOrCreate(apiUser.getId(), matchResult);
    }

    public Long acceptOffer(ApiUser apiUser, OfferAcceptAction action) {
        missionOfferAcceptValidator.validate(apiUser, action);
        return missionOfferAcceptHandler.accept(action);
    }
}
