package com.team.moodit.api.controller.v1.response;

public record MatchResultResponse(
        Long matchResultId,
        Long winnerPhotoId,
        String preferenceResultType, // 'TYPE_AND_DETAIL', 'TYPE_ONLY', 'TIE'
        String mainPreference,       // 상위 라벨 (예: AESTHETICS)
        String detailPreference,     // 하위 라벨 (예: DESIGN, null 가능)
        String message
) {
}
