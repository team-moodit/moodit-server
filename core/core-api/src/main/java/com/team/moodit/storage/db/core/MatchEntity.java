package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchState;
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
@Table(name = "`match`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchEntity extends BaseNoStatusEntity {
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchState state;

    @Column(nullable = false)
    private Integer initialImageCount;


    public void complete() {
        this.state = MatchState.DONE;
    }
}



