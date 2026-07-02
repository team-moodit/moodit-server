package com.team.moodit.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MatchProgressResult {
    private final String tournamentTitle;
    private final int totalRounds;
    private final int currentRound;
    private final int currentMatchOrder;
    private final MatchProgressInfo matchInfo;
    private final List<MatchProgressSelectedImage> selectedImages;

}

