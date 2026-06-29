package com.team.moodit.domain.enums;

import java.util.Set;
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

    public boolean support(PreferenceDetailType preferenceDetailType) {
        return detailTypes.contains(preferenceDetailType);
    }
}
