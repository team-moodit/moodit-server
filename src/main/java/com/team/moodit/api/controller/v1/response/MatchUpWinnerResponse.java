package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.MatchResult;

public record MatchUpWinnerResponse(
        Long matchUpWinnerId,
        Long winnerPhotoId,
        String preferenceResultType,
        String mainPreference,
        String detailPreference
) {
    // 1. 기존 MatchUpWinner용 생성자
    //
    public MatchUpWinnerResponse(MatchResult domain) {
        this(
                domain.getId(),
                domain.getRepresentativeMatchImageId(),
                domain.getPreferenceResult() != null && domain.getPreferenceResult().getResultType() != null ? domain.getPreferenceResult().getResultType().name() : null,
                domain.getPreferenceResult() != null && domain.getPreferenceResult().getPreferenceType() != null ? domain.getPreferenceResult().getPreferenceType().name() : null,
                domain.getPreferenceResult() != null && domain.getPreferenceResult().getPreferenceDetailType() != null ? domain.getPreferenceResult().getPreferenceDetailType().name() : null
        );
    }
}