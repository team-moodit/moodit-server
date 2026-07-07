package com.team.moodit.domain.match;

import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchResultService {
    private final MatchResultFinder matchResultFinder;

    public MatchResult getMatchResult(ApiUser apiUser, Long matchResultId) {
        return matchResultFinder.find(apiUser.getId(), matchResultId);
    }
}
