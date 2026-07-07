package com.team.moodit.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchImage {
    private Long id;
    private Long matchId;
    private Long fileId;
}
