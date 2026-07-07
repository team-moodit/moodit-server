package com.team.moodit.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CompletedMatches {
    private final List<CompletedMatch> content;
    private final long totalCount;
    private final boolean hasNext;
}
