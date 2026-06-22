package com.team.moodit.domain.match;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchCreator matchCreator;

    @Transactional
    public MatchCreateResult createMatch(Long userId, String title, List<Long> imageIds) {


        Match match = matchCreator.createMatch(userId, title, imageIds);


        return new MatchCreateResult(match.getId());
    }

}
