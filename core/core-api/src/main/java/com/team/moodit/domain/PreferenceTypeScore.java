package com.team.moodit.domain;

import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreferenceTypeScore {
    private PreferenceType preferenceType;
    private PreferenceDetailType preferenceDetailType;
    private int selectedCount;
    private int rank;

    public boolean isTopRank() {
        return rank == 1;
    }
}
