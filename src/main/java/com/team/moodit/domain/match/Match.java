package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.MatchEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Match {
    private Long id;
    private Long userId;
    private String title;
    private MatchState status;
    private Integer initialImageCount;


    public static Match from(MatchEntity matchEntity) {
        return new Match(
                matchEntity.getId(),
                matchEntity.getUserId(),
                matchEntity.getTitle(),
                matchEntity.getState(),
                matchEntity.getInitialImageCount()
        );
    }
}
