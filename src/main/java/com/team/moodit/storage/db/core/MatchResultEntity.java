package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.domain.enums.PreferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "match_result")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchResultEntity extends BaseNoStatusEntity {
    @Column(nullable = false)
    private Long matchId;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private Long representativeMatchImageId;
    @Column(nullable = false)
    private int roundCount;
    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR", nullable = false)
    private PreferenceResultType preferenceResultType;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private PreferenceType preferenceType;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private PreferenceDetailType preferenceDetailType;
}
