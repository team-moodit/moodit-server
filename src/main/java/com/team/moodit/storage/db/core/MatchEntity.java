package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchState;
import jakarta.persistence.*;
import lombok.*;


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
}
