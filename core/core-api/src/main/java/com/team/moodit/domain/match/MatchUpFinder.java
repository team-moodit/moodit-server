package com.team.moodit.domain.match;

import com.team.moodit.api.controller.v1.response.MatchStartResponse;
import com.team.moodit.api.controller.v1.response.MatchUpFlowResponse;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchVoteCandidateRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.file.FileReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchUpFinder {
    private final MatchUpRepository matchUpRepository;
    private final MatchRepository matchRepository;
    private final MatchImageRepository matchImageRepository;
    private final MatchVoteCandidateRepository matchVoteCandidateRepository;
    private final FileReader fileReader;

    @Transactional(readOnly = true)
    public MatchUpFlowResponse findNextMatchUp(Long matchId, Long userId) {

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (!match.getUserId().equals(userId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);

        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        MatchUpEntity nextTarget = matchUps.stream()
                .filter(matchUp -> !matchUp.isVoted())
                .findFirst()
                .orElse(null);

        boolean isTournamentCompleted = nextTarget == null;

        String roundTitle = "결승전";
        int displayMatchIndex = 1;
        int totalMatchUpInRound = 1;

        MatchStartResponse.NextMatchUpResponse nextMatchUpResponse = null;

        if (!isTournamentCompleted) {
            if (nextTarget.getCandidateBId() == null || nextTarget.getCandidateBId() == 0L) {
                throw new ApiException(ErrorType.INVALID_REQUEST);
            }

            List<MatchUpEntity> sameRoundMatchUps = matchUps.stream()
                    .filter(matchUp -> matchUp.getRoundNumber() == nextTarget.getRoundNumber())
                    .toList();

            List<MatchUpEntity> actualMatchesInRound = sameRoundMatchUps.stream()
                    .filter(matchUp -> matchUp.getCandidateBId() != null && matchUp.getCandidateBId() != 0L)
                    .toList();

            totalMatchUpInRound = actualMatchesInRound.size();

            long completedCountInRound = actualMatchesInRound.stream()
                    .filter(MatchUpEntity::isVoted)
                    .count();

            displayMatchIndex = (int) completedCountInRound + 1;

            int targetRoundNumber = nextTarget.getRoundNumber();
            int totalImages = match.getInitialImageCount();

            boolean isPerfectBracket =
                    totalImages == 4 || totalImages == 8 || totalImages == 16 || totalImages == 32;

            if (targetRoundNumber == 1 && !isPerfectBracket) {
                roundTitle = "예선전";
            } else if (totalMatchUpInRound == 1) {
                roundTitle = "결승전";
            } else if (totalMatchUpInRound == 2) {
                roundTitle = "준결승전";
            } else if (totalMatchUpInRound == 4) {
                roundTitle = "8강전";
            } else if (totalMatchUpInRound == 8) {
                roundTitle = "16강전";
            } else if (totalMatchUpInRound == 16) {
                roundTitle = "32강전";
            }

            MatchImageEntity candidateAImage = matchImageRepository.findById(nextTarget.getCandidateAId())
                    .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

            MatchImageEntity candidateBImage = matchImageRepository.findById(nextTarget.getCandidateBId())
                    .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

            String candidateAUrl = fileReader.getFile(candidateAImage.getFileId()).getUrl();
            String candidateBUrl = fileReader.getFile(candidateBImage.getFileId()).getUrl();

            nextMatchUpResponse = new MatchStartResponse.NextMatchUpResponse(
                    nextTarget.getId(),
                    new MatchStartResponse.CandidateResponse(
                            nextTarget.getCandidateAId(),
                            candidateAUrl
                    ),
                    new MatchStartResponse.CandidateResponse(
                            nextTarget.getCandidateBId(),
                            candidateBUrl
                    )
            );
        }

        long completedMatchCount = matchUps.stream()
                .filter(matchUp -> matchUp.getCandidateBId() != null && matchUp.getCandidateBId() != 0L)
                .filter(MatchUpEntity::isVoted)
                .count();

        int reasonRoundNumber = (int) completedMatchCount + 1;

        List<MatchStartResponse.ReasonResponse> reasons = matchVoteCandidateRepository
                .findByMatchIdAndRoundNumberOrderByDisplayOrderAsc(matchId, reasonRoundNumber)
                .stream()
                .map(candidate -> new MatchStartResponse.ReasonResponse(
                        candidate.getId(),
                        candidate.getContent()
                ))
                .toList();

        boolean finalCompletedStatus = isTournamentCompleted || "결승전".equals(roundTitle);

        return new MatchUpFlowResponse(
                match.getTitle(),
                roundTitle,
                displayMatchIndex,
                totalMatchUpInRound,
                finalCompletedStatus,
                nextMatchUpResponse,
                reasons
        );
    }
}
