package com.team.moodit.domain.report;

import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreferenceReport {
    private long totalMatchCount;
    private List<PreferenceCriterionShare> criteria;

    public PreferenceCriterionShare topCriterion() {
        return criteria.stream()
                .max(Comparator.comparingLong(PreferenceCriterionShare::getSelectedCount))
                .orElse(null);
    }
}
