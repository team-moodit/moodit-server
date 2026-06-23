package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.*;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchCreator {
    private final MatchRepository matchRepository;
    private final MatchImageRepository matchImageRepository;
    private final FileRepository fileRepository;

    @Transactional
    public Match createMatch(Long userId, String title, List<Long> imageIds) {


        validateImages(userId, imageIds);


        Match matchDomain = new Match( userId, title, MatchState.ING, imageIds.size());
        MatchEntity savedEntity = matchRepository.save(new MatchEntity(matchDomain));


        List<MatchImageEntity> imageEntities = imageIds.stream()
                .map(imageId -> new MatchImageEntity(savedEntity.getId(), imageId))
                .toList();
        matchImageRepository.saveAll(imageEntities);


        return savedEntity.toDomain();
    }

    private void validateImages(Long userId, List<Long> imageIds) {
        List<FileEntity> uploadedImages = fileRepository.findByUserIdAndIdIn(userId, imageIds);
        if (imageIds.size() != uploadedImages.size()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }
}