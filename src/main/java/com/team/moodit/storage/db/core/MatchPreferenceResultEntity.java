package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.PreferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "match_preference_result")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchPreferenceResultEntity extends BaseNoStatusEntity {
    private Long matchResultId;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private PreferenceType preferenceType;
    private int selectedCount;
    private int rank; // NOTE: 동률 시 같은 랭크로 지정
}
