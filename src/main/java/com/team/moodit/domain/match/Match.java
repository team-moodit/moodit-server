package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Match {
    private Long id;
    private Long userId;
    private String title;
    private MatchState state;
    private Integer initialImageCount;

    public Match(Long userId, String title, MatchState state, Integer initialImageCount) {
        this.userId = userId;
        this.title = title;
        this.state = state;
        this.initialImageCount = initialImageCount;
    }

}
