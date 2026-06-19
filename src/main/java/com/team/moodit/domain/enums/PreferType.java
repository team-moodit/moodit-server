package com.team.moodit.domain.enums;

import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PreferType {
    FITNESS( // 나와의 적합도
            Set.of(
                    PreferDetailType.BODY_FIT, // 신체적 특징
                    PreferDetailType.VIBE, // 추구미
                    PreferDetailType.MATCHABLE // 코디용이성
            )
    ),
    CONSISTENCE(Set.of()), // 지속성
    TREND(Set.of()), // 트렌드
    AESTHETICS( // 심미성
            Set.of(
                    PreferDetailType.DESIGN, // 디자인
                    PreferDetailType.MOOD, // 분위기
                    PreferDetailType.COLOR // 색감
            )
    );

    private final Set<PreferDetailType> detailTypes;

    public boolean support(PreferDetailType preferDetailType) {
        return detailTypes.contains(preferDetailType);
    }
}
