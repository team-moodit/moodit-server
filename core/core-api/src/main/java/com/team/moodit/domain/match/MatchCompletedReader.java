package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchResultEntity;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.file.FileReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchCompletedReader {

    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;
    private final MatchImageRepository matchImageRepository;
    private final FileReader fileReader;

    @Transactional(readOnly = true)
    public MatchCompletedResult getMatchCompleted(Long userId, Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (!match.getUserId().equals(userId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        MatchResultEntity matchResult = matchResultRepository.findByMatchId(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        MatchImageEntity winnerMatchImage = matchImageRepository.findById(
                        matchResult.getRepresentativeMatchImageId()
                )
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        MatchCompletedWinnerImage winnerImage = new MatchCompletedWinnerImage(
                winnerMatchImage.getId(),
                fileReader.getFile(winnerMatchImage.getFileId()).getUrl()
        );

        MatchCompletedPreferenceResult preferenceResult =
                new MatchCompletedPreferenceResult(
                        matchResult.getPreferenceResultType(),
                        matchResult.getPreferenceType(),
                        matchResult.getPreferenceDetailType()
                );

        List<MatchCompletedSelectedImage> selectedImages =
                matchImageRepository.findByMatchId(matchId).stream()
                        .map(matchImage -> new MatchCompletedSelectedImage(
                                matchImage.getId(),
                                fileReader.getFile(matchImage.getFileId()).getUrl()
                        ))
                        .toList();

        return new MatchCompletedResult(
                match.getTitle(),
                winnerImage,
                preferenceResult,
                matchResult.getCompletedAt(),
                selectedImages
        );
    }
}