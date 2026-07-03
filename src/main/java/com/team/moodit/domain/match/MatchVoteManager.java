package com.team.moodit.domain.match;

import com.team.moodit.api.controller.v1.response.VoteSaveResponse;
import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchVoteManager {
    private final MatchUpWinnerResultManager matchUpWinnerResultManager;
    private final MatchChoiceCreator matchChoiceCreator;
    private final MatchUpRepository matchUpRepository;
    private final MatchUpCreator matchUpCreator;

    @Transactional
    public VoteSaveResponse processVote(Long matchId, Long userId, VoteCommand command) {
        MatchUpEntity currentMatchUp = matchUpRepository.findByIdForUpdate(command.getMatchUpId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (!currentMatchUp.getMatchId().equals(matchId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        boolean alreadyVotedSameWinner = currentMatchUp.isSameWinner(command.getPhotoId());

        currentMatchUp.validateCandidate(command.getPhotoId());

        if (!alreadyVotedSameWinner) {
            currentMatchUp.updateWinner(command.getPhotoId());

            matchChoiceCreator.createChoice(
                    currentMatchUp.getId(),
                    command.getPhotoId(),
                    command.getReasonId()
            );

            matchUpRepository.saveAndFlush(currentMatchUp);
        }
        VoteSaveResponse response =
                handleRoundTransition(matchId, currentMatchUp.getRoundNumber());

        // 결승 종료 시 결과를 미리 생성
        if (response.isTournamentFinished()) {
            matchUpWinnerResultManager.getOrCreateMatchUpWinnerResult(
                    matchId,
                    userId
            );
        }

        return response;
    }

    @Transactional
    public VoteSaveResponse handleRoundTransition(Long matchId, int currentRound) {
        List<MatchUpEntity> allMatchUps = matchUpRepository.findByMatchId(matchId);

        List<MatchUpEntity> roundMatches = allMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound)
                .toList();

        int currentRoundOrder = (int) roundMatches.stream()
                .filter(MatchUpEntity::isVoted)
                .count();

        boolean isRoundCompleted = roundMatches.stream()
                .allMatch(MatchUpEntity::isVoted);

        Long nextMatchId = null;
        boolean isTournamentFinished = false;

        if (isRoundCompleted) {
            boolean isNextRoundExists = allMatchUps.stream()
                    .anyMatch(m -> m.getRoundNumber() == currentRound + 1);

            if (isNextRoundExists) {
                nextMatchId = findNextNeedVoteMatchId(allMatchUps);
            } else {
                List<Long> winners = roundMatches.stream()
                        .map(MatchUpEntity::getWinnerId)
                        .filter(id -> id != null && id != 0L)
                        .toList();

                if (winners.size() == 1) {
                    isTournamentFinished = true;
                } else {
                    List<MatchUpEntity> nextRoundMatches =
                            matchUpCreator.createNextRoundMatches(
                                    matchId,
                                    currentRound,
                                    winners
                            );

                    nextMatchId = matchUpRepository.saveAll(nextRoundMatches)
                            .get(0)
                            .getId();
                }
            }
        } else {
            nextMatchId = roundMatches.stream()
                    .filter(m -> m.getState() == MatchUpState.NEED_VOTE)
                    .min(Comparator.comparing(MatchUpEntity::getId))
                    .map(MatchUpEntity::getId)
                    .orElse(null);
        }

        return new VoteSaveResponse(
                nextMatchId,
                currentRound,
                currentRoundOrder,
                isTournamentFinished
        );
    }

    private Long findNextNeedVoteMatchId(List<MatchUpEntity> allMatchUps) {
        return allMatchUps.stream()
                .filter(m -> m.getState() == MatchUpState.NEED_VOTE)
                .min(Comparator.comparing(MatchUpEntity::getRoundNumber)
                        .thenComparing(MatchUpEntity::getId))
                .map(MatchUpEntity::getId)
                .orElse(null);
    }
}
