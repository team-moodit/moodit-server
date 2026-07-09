package com.team.moodit.domain.report;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreferenceReport {
    private long totalMatchCount;
    private PreferenceReportType resultType;
    private PreferenceCriterionShare topPreference;
    private PreferenceCriterionShare topPreferenceDetail;
    private List<PreferenceCriterionShare> criteria;

    public PreferenceCriterionShare topCriterion() {
        return topPreference;
    }
}
