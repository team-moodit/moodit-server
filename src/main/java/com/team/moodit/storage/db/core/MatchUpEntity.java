package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchUpState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="match_up")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchUpEntity extends BaseNoStatusEntity {
    private Long matchId;
    private int roundNumber;
    private Long candidateAId;
    private Long candidateBId;
    private Long winnerId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private MatchUpState state;

}
