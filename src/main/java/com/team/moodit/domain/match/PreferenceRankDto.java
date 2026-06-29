package com.team.moodit.domain.match;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class PreferenceRankDto {
    private final String label;
    private final int rank;
    private final int count;
}