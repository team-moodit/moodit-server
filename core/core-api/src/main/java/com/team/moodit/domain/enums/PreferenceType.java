package com.team.moodit.domain.enums;

import java.util.Set;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PreferenceType {
    FITNESS(
            "나와의 적합도",
            Set.of(
                    PreferenceDetailType.BODY_FIT, // 신체적 특징
                    PreferenceDetailType.VIBE, // 추구미
                    PreferenceDetailType.MATCHABLE // 코디용이성
            )
    ),
    CONSISTENCE("지속성", Set.of()),
    TREND("트렌드", Set.of()),
    AESTHETICS(
            "심미성",
            Set.of(
                    PreferenceDetailType.DESIGN, // 디자인
                    PreferenceDetailType.MOOD, // 분위기
                    PreferenceDetailType.COLOR // 색감
            )
    );

    private final String title;
    private final Set<PreferenceDetailType> detailTypes;

    public boolean hasDetail() {
        return !detailTypes.isEmpty();
    }

    public static PreferenceType from(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        for (PreferenceType type : PreferenceType.values()) {
            // 1. DB에 저장된 영문 이름(FITNESS, TREND 등)과 매칭 (대소문자 무시)
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
            // 2. 한글 타이틀("나와의 적합도", "지속성" 등)과 매칭
            if (type.getTitle().equals(value.trim())) {
                return type;
            }
        }
        throw new ApiException(ErrorType.INVALID_REQUEST);
    }
}
