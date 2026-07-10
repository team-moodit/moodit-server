package com.team.moodit.domain.enums;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PreferenceDetailType {
    BODY_FIT("신체적 특징", "신체와의 적합도"),
    VIBE("추구미", "추구미"),
    MATCHABLE("코디 용이성", "코디 용이성"),
    DESIGN("디자인", "디자인"),
    MOOD("분위기", "분위기"),
    COLOR("색감", "색감");

    private final String title;
    private final String insightTitle;

    public static PreferenceDetailType from(String value) {
        try {
            return PreferenceDetailType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}
