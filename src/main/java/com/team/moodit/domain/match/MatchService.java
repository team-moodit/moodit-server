package com.team.moodit.domain.match;

import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchCreator matchCreator;

    public Long createMatch(ApiUser apiUser, String title, List<Long> imageIds) {
        return matchCreator.create(apiUser.getId(), title, imageIds);
    }
}
