package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchImageEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchImageReader {
    private final MatchImageRepository matchImageRepository;

    public MatchImage getMatchImage(Long matchImageId) {
        MatchImageEntity entity = matchImageRepository.findById(matchImageId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        return new MatchImage(
                entity.getMatchId(),
                entity.getFileId()
        );
    }
}
