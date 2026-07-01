package com.team.moodit.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CompletedMatch {

    private Long matchId;
    private String title;

    private Long winnerImageId;
    private String winnerImageUri;

    private LocalDate completedAt;
}