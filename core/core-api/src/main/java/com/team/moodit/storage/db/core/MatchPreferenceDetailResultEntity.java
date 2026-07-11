package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "match_preference_detail_result",
        indexes = {
                @Index(
                        name = "idx_match_preference_detail_result_match_result_id",
                        columnList = "matchResultId"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchPreferenceDetailResultEntity extends BaseNoStatusEntity {

    @Column(nullable = false)
    private Long matchResultId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR")
    private PreferenceType preferenceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR")
    private PreferenceDetailType preferenceDetailType;

    @Column(nullable = false)
    private int selectedCount;

    @Column(nullable = false)
    private int rank;

    public MatchPreferenceDetailResultEntity(
            Long matchResultId,
            PreferenceType preferenceType,
            PreferenceDetailType preferenceDetailType,
            int selectedCount,
            int rank
    ) {
        this.matchResultId = matchResultId;
        this.preferenceType = preferenceType;
        this.preferenceDetailType = preferenceDetailType;
        this.selectedCount = selectedCount;
        this.rank = rank;
    }
}