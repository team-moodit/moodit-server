package com.team.moodit.domain.report;

import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PreferenceCriterionShare {
    private PreferenceType type;
    private PreferenceDetailType detailType;
    private String title;
    private long selectedCount;
    private int percentage;

    public static PreferenceCriterionShare preference(
            PreferenceType type,
            long selectedCount,
            int percentage
    ) {
        return new PreferenceCriterionShare(
                type,
                null,
                type.getTitle(),
                selectedCount,
                percentage
        );
    }

    public static PreferenceCriterionShare detail(
            PreferenceType type,
            PreferenceDetailType detailType,
            long selectedCount,
            int percentage
    ) {
        return new PreferenceCriterionShare(
                type,
                detailType,
                detailType.getTitle(),
                selectedCount,
                percentage
        );
    }
}
