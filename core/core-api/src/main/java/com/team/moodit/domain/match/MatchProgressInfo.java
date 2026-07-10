package com.team.moodit.domain.match;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchProgressInfo {
    private int totalImageCount;
    private String lastPlayedAt;
}
