package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchCreator {
    private final MatchRepository matchRepository;
    private final MatchImageRepository matchImageRepository;

    public Match createMatch(Long userId, String title, List<Long> imageIds) {
        MatchEntity entity = MatchEntity.builder()
                .userId(userId)
                .title(title)
                .state(MatchState.ING)
                .initialImageCount(imageIds.size())
                .build();

        MatchEntity savedMatch = matchRepository.save(entity);

        List<MatchImageEntity> imageEntities = imageIds.stream()
                .map(fileId -> new MatchImageEntity(savedMatch.getId(), fileId))
                .toList();
        matchImageRepository.saveAll(imageEntities);

        return Match.from(savedMatch);
    }


    }

