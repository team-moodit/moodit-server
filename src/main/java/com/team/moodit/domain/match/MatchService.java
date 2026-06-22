package com.team.moodit.domain.match;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchCreator matchCreator;

    public MatchCreateResult createMatch(Long userId, String title, List<Long> fileIds) {


        Match match = matchCreator.createMatch(userId, title, fileIds.size());


        matchCreator.createMatchImages(match.getId(), fileIds);


        return new MatchCreateResult(match.getId());
    }

}
