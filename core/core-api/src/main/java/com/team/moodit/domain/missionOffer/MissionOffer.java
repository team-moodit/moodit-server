package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.enums.MissionOfferState;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionOffer {
    private Long id;
    private Long matchResultId;
    private Long userId;
    private List<MissionCandidate> candidates;
    private MissionOfferState state;

    public MissionCandidate getCandidate(Long candidateId) {
        return candidates.stream()
                .filter(it -> it.getId().equals(candidateId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));
    }
}
