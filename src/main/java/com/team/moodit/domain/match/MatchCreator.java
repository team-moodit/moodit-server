package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.*;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchCreator {
    private final MatchRepository matchRepository;
    private final MatchImageRepository matchImageRepository;
    private final FileRepository fileRepository;

    public Match createMatch(Long userId, String title, List<Long> imageIds) {

        List<FileEntity> uploadedImages = fileRepository.findByUserIdAndIdIn(userId, imageIds);

        if (imageIds.size() != uploadedImages.size()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        MatchEntity entity = MatchEntity.builder()
                .userId(userId)
                .title(title)
                .state(MatchState.ING)
                .initialImageCount(imageIds.size())
                .build();

        MatchEntity savedMatch = matchRepository.save(entity);

        List<MatchImageEntity> imageEntities = imageIds.stream()
                .map(imageId -> new MatchImageEntity(savedMatch.getId(), imageId))
                .toList();
        matchImageRepository.saveAll(imageEntities);

        return Match.from(savedMatch);
    }


}

