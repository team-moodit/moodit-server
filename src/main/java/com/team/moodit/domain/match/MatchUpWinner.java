package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceResultType;
import lombok.Getter;

@Getter
public class MatchUpWinner {
    private final Long matchUpWinnerId; // 최종 승자 결과 엔티티 ID
    private final Long winnerPhotoId;   // 최종 우승 사진 ID
    private final PreferenceResultType preferenceResultType;
    private final String mainPreference;
    private final String detailPreference;

    public MatchUpWinner(Long matchUpWinnerId, Long winnerPhotoId, PreferenceResultType preferenceResultType, String mainPreference, String detailPreference) {
        this.matchUpWinnerId = matchUpWinnerId;
        this.winnerPhotoId = winnerPhotoId;
        this.preferenceResultType = preferenceResultType;
        this.mainPreference = mainPreference;
        this.detailPreference = detailPreference;
    }
}