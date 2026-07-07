package com.team.moodit.domain.match;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchResult {
    private Long id;
    private Long matchId;
    private String title;
    private Long representativeMatchImageId;
    private int roundCount;
    private LocalDateTime completedAt;
    private MatchPreferenceResult preferenceResult;
}
