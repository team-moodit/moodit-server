package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.domain.match.Match;
import com.team.moodit.support.file.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record MatchResponse(
        Long matchId,
        String representativeImageUrl,
        String title,
        MatchState state,
        LocalDateTime doneAt
) {
    public static List<MatchResponse> of(
            List<Match> matches,
            Map<Long, File> matchImageFileMap
    ) {
        return matches.stream()
                .map(it -> new MatchResponse(
                        it.getId(),
                        matchImageFileMap.get(it.getRepresentativeImageId()).getUrl(),
                        it.getTitle(),
                        it.getState(),
                        it.getDoneAt()
                ))
                .toList();
    }
}
