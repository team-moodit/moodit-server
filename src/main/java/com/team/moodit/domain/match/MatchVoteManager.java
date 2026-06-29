package com.team.moodit.domain.match;

import com.team.moodit.api.controller.v1.response.VoteSaveResponse;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchVoteManager {

    private final MatchChoiceCreator matchChoiceCreator;
    private final MatchUpRepository matchUpRepository;
    private final MatchUpCreator matchUpCreator;

    /**
     * 투표 처리 (개별 경기는 엔티티의 @Version 낙관적 락으로 동시성 제어)
     */
    @Transactional
    public VoteSaveResponse processVote(Long matchId, VoteCommand command) {
        MatchUpEntity currentMatchUp = matchUpRepository.findById(command.getMatchUpId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (!currentMatchUp.getMatchId().equals(matchId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        currentMatchUp.validateCandidate(command.getPhotoId());
        currentMatchUp.updateWinner(command.getPhotoId());

        matchChoiceCreator.createChoice(
                currentMatchUp.getId(),
                command.getPhotoId(),
                command.getReasonId()
        );

        // 현재 경기에 대한 낙관적 락 버전을 즉시 올리고 플러시
        matchUpRepository.saveAndFlush(currentMatchUp);

        // 라운드 전환 로직 수행 (비관적 락으로 줄 세우기 진입)
        return handleRoundTransition(matchId, currentMatchUp.getRoundNumber());
    }

    /**
     * 라운드 전환 및 다음 경기 계산 (비관적 쓰기 락을 통한 대진표 중복 생성 방지)
     */
    public VoteSaveResponse handleRoundTransition(Long matchId, int currentRound) {
        //  일반 조회를 '비관적 쓰기 락'이 걸린 조회 쿼리로 교체!
        List<MatchUpEntity> freshMatchUps = matchUpRepository.findByMatchId(matchId);

        List<MatchUpEntity> actualMatchesInRound = freshMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound)
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        if (actualMatchesInRound.isEmpty()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        int currentRoundOrder = (int) actualMatchesInRound.stream()
                .filter(MatchUpEntity::isVoted)
                .count();

        boolean isRoundCompleted = actualMatchesInRound.stream()
                .allMatch(MatchUpEntity::isVoted);

        Long nextMatchId = null;
        boolean isTournamentFinished = false;

        if (isRoundCompleted) {
            //  Double-Checked Locking: 다른 쓰레드가 찰나의 차이로 이미 다음 라운드를 생성했는지 락 내부에서 재검증
            boolean isNextRoundAlreadyCreated = freshMatchUps.stream()
                    .anyMatch(m -> m.getRoundNumber() == currentRound + 1);

            if (isNextRoundAlreadyCreated) {
                // 이미 생성되어 있다면 새로 만들지 않고, 해당 라운드의 첫 번째 경기 ID를 반환
                nextMatchId = freshMatchUps.stream()
                        .filter(m -> m.getRoundNumber() == currentRound + 1)
                        .findFirst()
                        .map(MatchUpEntity::getId)
                        .orElse(null);
            } else {
                List<Long> winnersImageIds = freshMatchUps.stream()
                        .filter(m -> m.getRoundNumber() == currentRound)
                        .map(MatchUpEntity::getWinnerId)
                        .filter(id -> id != null && id != 0L)
                        .toList();

                if (winnersImageIds.size() == 1) {
                    isTournamentFinished = true;
                } else {
                    List<MatchUpEntity> nextRoundMatches = matchUpCreator.createNextRoundMatches(matchId, currentRound, winnersImageIds);
                    nextMatchId = matchUpRepository.saveAll(nextRoundMatches).get(0).getId();
                }
            }
        } else {
            nextMatchId = actualMatchesInRound.stream()
                    .filter(m -> !m.isVoted())
                    .findFirst()
                    .map(MatchUpEntity::getId)
                    .orElse(null);
        }

        return new VoteSaveResponse(nextMatchId, currentRound, currentRoundOrder, isTournamentFinished);
    }
}