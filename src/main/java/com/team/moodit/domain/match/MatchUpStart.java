package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchVoteEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class MatchUpStart {
    private final String tournamentTitle;
    private final int totalRounds;
    private final int currentRound;
    private final String roundName;
    private final Long candidateAId;
    private final String candidateAUrl;
    private final Long candidateBId;
    private final String candidateBUrl;
    private final List<MatchVoteEntity> reasons;
}