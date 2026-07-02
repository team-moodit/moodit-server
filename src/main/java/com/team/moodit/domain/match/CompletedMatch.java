package com.team.moodit.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CompletedMatch {
    private final Long matchId;
    private final String title;
    private final Long winnerImageId;
    private final String winnerImageUri;
    private final LocalDate completedAt;
}
