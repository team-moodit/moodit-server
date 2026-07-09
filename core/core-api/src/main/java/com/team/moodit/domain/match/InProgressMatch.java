package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchResumeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InProgressMatch {
    private final Long matchId;
    private final Long matchResultId;
    private final String title;
    private final int currentRound;
    private final int totalRound;
    private final LocalDateTime lastPlayedAt;
    private final MatchResumeType resumeType;
}