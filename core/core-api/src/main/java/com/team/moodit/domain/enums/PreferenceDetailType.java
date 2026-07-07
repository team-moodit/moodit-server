package com.team.moodit.domain.enums;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;

public enum PreferenceDetailType {
    BODY_FIT,
    VIBE,
    MATCHABLE,
    DESIGN,
    MOOD,
    COLOR;

    public static PreferenceDetailType from(String value) {
        try {
            return PreferenceDetailType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
