package com.team.moodit.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MatchCompletedResult {
    private String title;
    private MatchCompletedWinnerImage winnerImage;
    private MatchCompletedPreferenceResult preferenceResult;
    private LocalDateTime completedAt;
    List<MatchCompletedSelectedImage> selectedImages;
}
