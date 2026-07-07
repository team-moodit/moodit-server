package com.team.moodit.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InProgressMatches {
    private final List<InProgressMatch> content;
    private final long totalCount;
    private final boolean hasNext;
}
