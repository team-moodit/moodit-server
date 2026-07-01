package com.team.moodit.domain.match;

import com.team.moodit.support.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class MatchTab {
    private Page<InProgressMatch> inProgressMatches;
    private Page<CompletedMatch> completedMatches;
}