package com.team.moodit.domain.match;

import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchCreator matchCreator;
    private final MatchResultReader matchResultReader;

    public Long createMatch(ApiUser apiUser, NewMatch newMatch, List<Long> imageIds) {
        return matchCreator.create(apiUser.getId(), newMatch, imageIds);
    }

    public MatchResult getMatchResult(ApiUser apiUser, Long matchId) {
        return matchResultReader.getMatchResult(apiUser.getId(), matchId);
    }
}
