package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.FileEntity;
import com.team.moodit.storage.db.core.FileRepository;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
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
        MatchEntity savedMatch = matchRepository.save(
                new MatchEntity(
                        userId,
                        title,
                        MatchState.ING,
                        imageIds.size()
                )
        );

        List<FileEntity> uploadedImages = fileRepository.findByUserIdAndIdIn(userId, imageIds);
        if (imageIds.size() != uploadedImages.size()) throw new ApiException(ErrorType.INVALID_REQUEST);

        matchImageRepository.saveAll(
                uploadedImages.stream().map(it ->
                        new MatchImageEntity(
                                savedMatch.getId(),
                                it.getId()
                        )
                ).toList()
        );

        return savedMatch.toDomain();
    }
}
