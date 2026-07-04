package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceDetailType;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class PreferenceRankDto {
    private String label;
    private PreferenceDetailType preferenceDetailType;
    private int rank;
    private int count;

}