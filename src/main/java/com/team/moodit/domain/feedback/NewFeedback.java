package com.team.moodit.domain.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewFeedback {
    private double satisfactionScore;
    private String dissatisfactionReason;
}
