package com.team.moodit.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
public class InProgressMatch {
    private Long matchId;
    private String title;
    private int currentRound;
    private int totalRound;
    private LocalDateTime lastPlayedAt;
}
