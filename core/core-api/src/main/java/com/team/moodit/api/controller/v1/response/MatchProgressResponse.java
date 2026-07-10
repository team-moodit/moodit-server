package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.match.MatchProgressInfo;
import com.team.moodit.domain.match.MatchProgressResult;
import com.team.moodit.domain.match.MatchProgressSelectedImage;

import java.util.List;

public record MatchProgressResponse(
        String tournamentTitle,
        int totalRounds,
        int currentRound,
        int currentMatchOrder,
        MatchInfo matchInfo,
        List<SelectedImage> selectedImages
) {

    public static MatchProgressResponse from(MatchProgressResult result) {
        return new MatchProgressResponse(
                result.getTournamentTitle(),
                result.getTotalRounds(),
                result.getCurrentRound(),
                result.getCurrentMatchOrder(),
                MatchInfo.from(result.getMatchInfo()),
                result.getSelectedImages().stream()
                        .map(SelectedImage::from)
                        .toList()
        );
    }

    public record MatchInfo(
            int totalImageCount,
            String LastPlayedAt
    ) {
        private static MatchInfo from(MatchProgressInfo matchProgressInfo) {
            return new MatchInfo(
                    matchProgressInfo.getTotalImageCount(),
                    matchProgressInfo.getLastPlayedAt()
            );
        }
    }

    public record SelectedImage(
            Long id,
            String photoUri
    ) {
        private static SelectedImage from(MatchProgressSelectedImage matchProgressSelectedImage) {
            return new SelectedImage(
                    matchProgressSelectedImage.getId(),
                    matchProgressSelectedImage.getPhotoUri()
            );
        }
    }
}