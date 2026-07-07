package com.team.moodit.domain.report;

import com.team.moodit.domain.enums.PreferenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreferenceCriterionShare {
    PreferenceType type;
    long selectedCount;
    int percentage;
}
