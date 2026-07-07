package com.team.moodit.domain.match;

import com.team.moodit.support.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchTab {
    private final Page<InProgressMatch> inProgressMatches;
    private final Page<CompletedMatch> completedMatches;
}
