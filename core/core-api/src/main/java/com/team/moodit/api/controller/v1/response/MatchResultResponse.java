package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.support.file.File;
import java.time.LocalDateTime;

public record MatchResultResponse(
        Long matchResultId,
        String matchTitle,
        String matchRepresentativeImageUrl, // 매치 대표 이미지
        PreferenceResultType preferenceResultType,
        String matchPreferenceTypeTitle, // 매치 선호 결과 (나와의 적합성)
        int matchRoundCount,
        LocalDateTime matchCompletedAt
) {
    public static MatchResultResponse of(
            MatchResult result,
            File matchRepresentativeImageFile
    ) {
        PreferenceType preferenceType = result.getPreferenceResult().getPreferenceType();

        return new MatchResultResponse(
                result.getMatchId(),
                result.getTitle(),
                matchRepresentativeImageFile.getUrl(),
                result.getPreferenceResult().getResultType(),
                preferenceType != null ? preferenceType.getTitle() : null,
                result.getRoundCount(),
                result.getCompletedAt()
        );
    }
}
