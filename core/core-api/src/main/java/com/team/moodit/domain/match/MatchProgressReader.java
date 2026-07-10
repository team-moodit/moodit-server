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

import java.time.LocalDateTime;
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

        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        /*
         * 실제 투표가 완료된 MatchUp 중 가장 최근 updatedAt을 마지막 진행일로 사용한다.
         *
         * 아직 한 번도 투표하지 않았다면 COMPLETED 상태가 없으므로
         * 매치 생성일을 fallback 값으로 사용한다.
         *
         * SKIPPED는 사용자 투표가 아닌 자동 진출이므로 제외한다.
         */
        LocalDateTime lastPlayedAt = matchUps.stream()
                .filter(matchUp -> matchUp.getState() == MatchUpState.COMPLETED)
                .map(MatchUpEntity::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(match.getCreatedAt());

        /*
         * 아직 투표할 MatchUp이 있다면 가장 앞선 roundNumber를 사용한다.
         *
         * 모든 MatchUp이 완료되어 NEED_VOTE가 없다면,
         * 예외 대신 가장 큰 roundNumber를 사용한다.
         */
        int currentRoundNumber = matchUps.stream()
                .filter(matchUp -> matchUp.getState() == MatchUpState.NEED_VOTE)
                .map(MatchUpEntity::getRoundNumber)
                .min(Integer::compareTo)
                .orElseGet(() -> matchUps.stream()
                        .map(MatchUpEntity::getRoundNumber)
                        .max(Integer::compareTo)
                        .orElseThrow(() ->
                                new ApiException(ErrorType.INVALID_REQUEST)
                        ));

        int currentRound = calculateCurrentRound(
                selectedImages.size(),
                totalRound,
                currentRoundNumber
        );

        long completedCount = matchUps.stream()
                .filter(matchUp ->
                        matchUp.getRoundNumber() == currentRoundNumber
                )
                .filter(matchUp ->
                        matchUp.getState() == MatchUpState.COMPLETED
                )
                .count();

        int currentMatchOrder = (int) completedCount + 1;

        return new MatchProgressResult(
                match.getTitle(),
                totalRound,
                currentRound,
                Math.min(currentMatchOrder, matchUps.size()),
                new MatchProgressInfo(
                        selectedImages.size(),
                        lastPlayedAt.format(DATE_FORMATTER)
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
            int divisor = 1 << (safeRoundNumber - 1);
            return totalRound / Math.max(divisor, 1);
        }

        if (safeRoundNumber == 1) {
            return totalRound;
        }

        int divisor = 1 << (safeRoundNumber - 2);
        return totalRound / Math.max(divisor, 1);
    }
}