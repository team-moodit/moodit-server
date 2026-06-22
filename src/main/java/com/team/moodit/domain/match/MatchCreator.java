package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchStatus;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchCreator {
    private final MatchRepository matchRepository;
    private final MatchImageRepository matchImageRepository;

    public Match createMatch(Long userId, String title, int imageCount) {
        MatchEntity entity = MatchEntity.builder()
                .userId(userId)
                .title(title)
                .status(MatchStatus.ING)
                .initialImageCount(imageCount)
                .build();

        return Match.from(matchRepository.save(entity));
    }

}
