package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.file.FileReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchProgressReader {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final MatchRepository matchRepository;
    private final MatchImageRepository matchImageRepository;
    private final MatchUpRepository matchUpRepository;
    private final FileReader fileReader;

    public MatchProgressResult getMatchProgress(Long userId, Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (!match.getUserId().equals(userId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        List<MatchImageEntity> matchImages =
                matchImageRepository.findByMatchId(matchId);

        List<MatchProgressSelectedImage> selectedImages = matchImages.stream()
                .map(matchImage -> new MatchProgressSelectedImage(
                        matchImage.getId(),
                        fileReader.getFile(matchImage.getFileId()).getUrl()
                ))
                .toList();

        int totalRound = calculateTotalRound(selectedImages.size());

        List<MatchUpEntity> matchUps =
                matchUpRepository.findByMatchIdOrderByRoundNumberAscIdAsc(matchId);

        int currentRoundNumber = matchUps.stream()
                .filter(matchUp -> matchUp.getState() == MatchUpState.NEED_VOTE)
                .map(MatchUpEntity::getRoundNumber)
                .min(Integer::compareTo)
                .orElse(1);

        int currentRound = calculateCurrentRound(
                selectedImages.size(),
                totalRound,
                currentRoundNumber
        );

        long completedCount = matchUps.stream()
                .filter(matchUp -> matchUp.getRoundNumber() == currentRoundNumber)
                .filter(matchUp -> matchUp.getState() == MatchUpState.COMPLETED)
                .count();

        int currentMatchOrder = (int) completedCount + 1;

        return new MatchProgressResult(
                match.getTitle(),
                totalRound,
                currentRound,
                currentMatchOrder,
                new MatchProgressInfo(
                        selectedImages.size(),
                        match.getCreatedAt().format(DATE_FORMATTER)
                ),
                selectedImages
        );
    }

    private int calculateTotalRound(int imageCount) {
        return Integer.highestOneBit(imageCount);
    }

    private int calculateCurrentRound(
            int imageCount,
            int totalRound,
            int currentRoundNumber
    ) {
        boolean hasPreliminaryRound = imageCount != totalRound;

        if (!hasPreliminaryRound) {
            return totalRound / (int) Math.pow(2, currentRoundNumber - 1);
        }

        if (currentRoundNumber == 1) {
            return totalRound;
        }

        return totalRound / (int) Math.pow(2, currentRoundNumber - 2);
    }
}