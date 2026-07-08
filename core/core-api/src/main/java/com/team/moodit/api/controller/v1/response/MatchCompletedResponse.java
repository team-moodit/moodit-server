package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.MatchCompletedPreferenceResult;
import com.team.moodit.domain.match.MatchCompletedResult;
import com.team.moodit.domain.match.MatchCompletedSelectedImage;
import com.team.moodit.domain.match.MatchCompletedWinnerImage;

import java.time.LocalDateTime;
import java.util.List;

public record MatchCompletedResponse(
        String title,
        WinnerImage winnerImage,
        PreferenceResult preferenceResult,
        LocalDateTime completedAt,
        String preferenceTitle,
        List<SelectedImage> selectedImages
) {

    public static MatchCompletedResponse from(MatchCompletedResult result) {
        return new MatchCompletedResponse(
                result.getTitle(),
                WinnerImage.from(result.getWinnerImage()),
                PreferenceResult.from(result.getPreferenceResult()),
                result.getCompletedAt(),
                resolvePreferenceTitle(result.getPreferenceResult()),
                result.getSelectedImages().stream()
                        .map(SelectedImage::from)
                        .toList()
        );
    }

    private static String resolvePreferenceTitle(MatchCompletedPreferenceResult result) {
        if (result == null || result.getPreferenceResultType() == null) {
            return null;
        }

        return switch (result.getPreferenceResultType()) {
            case TYPE_ONLY, TYPE_AND_DETAIL -> result.getPreferenceType() == null
                    ? null
                    : result.getPreferenceType().getTitle();

            case TIE -> null;
        };
    }

    public record WinnerImage(
            Long id,
            String photoUri
    ) {
        public static WinnerImage from(MatchCompletedWinnerImage matchCompletedWinnerImage) {
            return new WinnerImage(
                    matchCompletedWinnerImage.getId(),
                    matchCompletedWinnerImage.getImageuri()
            );
        }
    }

    public record PreferenceResult(
            String preferenceResultType,
            String preferenceType,
            String preferenceDetailType
    ) {
        public static PreferenceResult from(MatchCompletedPreferenceResult matchCompletedPreferenceResult) {
            return new PreferenceResult(
                    matchCompletedPreferenceResult.getPreferenceResultType().name(),
                    matchCompletedPreferenceResult.getPreferenceType() == null
                            ? null
                            : matchCompletedPreferenceResult.getPreferenceType().name(),
                    matchCompletedPreferenceResult.getPreferenceDetailType() == null
                            ? null
                            : matchCompletedPreferenceResult.getPreferenceDetailType().name()
            );
        }
    }

    public record SelectedImage(
            Long id,
            String photoUri
    ) {
        public static SelectedImage from(MatchCompletedSelectedImage matchCompletedSelectedImage) {
            return new SelectedImage(
                    matchCompletedSelectedImage.getId(),
                    matchCompletedSelectedImage.getImaguri()
            );
        }
    }
}
