package com.team.moodit.domain.enums;

import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PreferType {
    FITNESS(
            Set.of(
                    PreferDetailType.BODY_FIT,
                    PreferDetailType.VIBE,
                    PreferDetailType.MATCHABLE
            )
    ),
    CONSISTENCE(Set.of()),
    TREND(Set.of()),
    AESTHETICS(
            Set.of(
                    PreferDetailType.DESIGN,
                    PreferDetailType.MOOD,
                    PreferDetailType.COLOR
            )
    );

    private final Set<PreferDetailType> detailTypes;

    public boolean support(PreferDetailType preferDetailType) {
        return detailTypes.contains(preferDetailType);
    }
}
