package com.team.moodit.domain.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionSatisfactionSummary {
    private long feedbackCount;
    private double averageScore;
}
