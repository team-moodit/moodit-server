package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceResultType;
import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
public class MatchPreferenceAnalysis {
    private final PreferenceResultType resultType;
    private final String mainPref;
    private final String detailPref;
    private final List<PreferenceRankDto> ranks;
}