package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.domain.match.Match;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@Builder
@Table(name = "`match`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchEntity extends BaseNoStatusEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private MatchState state;


    @Column(nullable = false)
    private Integer initialImageCount;


    public MatchEntity(Match match) {
        this.userId = match.getUserId();
        this.title = match.getTitle();
        this.state = match.getState();
        this.initialImageCount = match.getInitialImageCount();
    }

    public Match toDomain() {
        return new Match(
                this.getId(),
                this.userId,
                this.title,
                this.state,
                this.initialImageCount
        );
    }





}
