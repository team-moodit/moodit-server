package com.team.moodit.domain.feedback;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewFeedback {
    private double satisfactionScore;
    private List<String> dissatisfactionReasons;
}
