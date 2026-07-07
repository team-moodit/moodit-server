package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.domain.enums.PreferenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchCompletedPreferenceResult {
    private PreferenceResultType preferenceResultType;
    private PreferenceType preferenceType;
    private PreferenceDetailType preferenceDetailType;
}
