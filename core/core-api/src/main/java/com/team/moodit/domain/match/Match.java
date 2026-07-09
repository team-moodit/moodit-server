package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Match {
    private Long id;
    private Long userId;
    private Long representativeImageId;
    private String title;
    private MatchState state;
    private LocalDateTime doneAt;
}
