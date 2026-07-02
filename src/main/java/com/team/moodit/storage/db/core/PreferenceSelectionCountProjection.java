package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.PreferenceType;

public interface PreferenceSelectionCountProjection {
    PreferenceType getPreferenceType();
    Long getCount();
}
