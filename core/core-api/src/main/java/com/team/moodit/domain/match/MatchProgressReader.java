package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
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
        MatchEntity match = matchRepository.findByIdAndUserId(matchId, userId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (match.getState() == MatchState.DONE) {
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
                .orElseGet(() -> matchUps.stream()
                        .map(MatchUpEntity::getRoundNumber)
                        .max(Integer::compareTo)
                        .orElseThrow(() -> new ApiException(ErrorType.INVALID_REQUEST)));
            // 모든 매치업을 완료하면 NEED_VOTE가 없기 때문에 없는 경우에는 예외 대신 최대 라운드를 반환하도록 수정했습니다. ex 8강

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
                currentRound, // 몇강
                Math.min(currentMatchOrder, matchUps.size()), // 몇 번째 매치
                new MatchProgressInfo(
                        selectedImages.size(),
                        match.getCreatedAt().format(DATE_FORMATTER)
                ),
                selectedImages
        );
    }

    private int calculateTotalRound(int imageCount) {
        if (imageCount <= 0) {
            return 0;
        }

        return Integer.highestOneBit(imageCount);
    }

    private int calculateCurrentRound(
            int imageCount,
            int totalRound,
            int currentRoundNumber
    ) {
        if (totalRound <= 0) {
            return 0;
        }

        int safeRoundNumber = Math.max(currentRoundNumber, 1);
        boolean hasPreliminaryRound = imageCount != totalRound;

        if (!hasPreliminaryRound) {
            return totalRound / (int) Math.pow(2, safeRoundNumber - 1);
        }

        if (safeRoundNumber == 1) {
            return totalRound;
        }

        return totalRound / (int) Math.pow(2, safeRoundNumber - 2);
    }
}
