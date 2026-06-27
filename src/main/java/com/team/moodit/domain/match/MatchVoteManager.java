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

    @Transactional
    public VoteSaveResponse processVote(Long matchId, VoteCommand command) {

        // 1. 데이터 조회 시점에 비관적 락을 걸어 동시성 요청 제어
        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchIdWithLock(matchId);
        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        // 2. 현재 투표해야 하는 진행 중인 대진 탐색
        MatchUpEntity currentMatchUp = matchUps.stream()
                .filter(m -> !m.isVoted())
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_REQUEST));

        // 3. 비즈니스 검증 및 승자 확정
        currentMatchUp.validateCandidate(command.getPhotoId());
        currentMatchUp.updateWinner(command.getPhotoId());
        matchUpRepository.saveAndFlush(currentMatchUp);

        // 4. 투표 사유 기록
        matchChoiceCreator.createChoice(
                currentMatchUp.getId(),
                command.getPhotoId(),
                command.getReasonId()
        );

        // 5. 최신 대진표 리스트 다시 불러오기
        List<MatchUpEntity> freshMatchUps = matchUpRepository.findByMatchId(matchId);

        // 6. 메타데이터 계산
        int currentRound = currentMatchUp.getRoundNumber();
        int currentRoundOrder = (int) freshMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound && m.isVoted())
                .count();

        boolean isRoundCompleted = freshMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound)
                .allMatch(MatchUpEntity::isVoted);

        Long nextMatchId = null;
        boolean isTournamentFinished = false;

        // 7. 대진 흐름 제어 (라운드 종료 체크)
        if (isRoundCompleted) {
            List<Long> winnersImageIds = freshMatchUps.stream()
                    .filter(m -> m.getRoundNumber() == currentRound)
                    .map(MatchUpEntity::getWinnerId)
                    .toList();

            // 승자가 1명뿐이라면 토너먼트 최종 종료
            if (winnersImageIds.size() == 1) {
                isTournamentFinished = true;
            } else {
                // 승자가 여러 명이면 다음 라운드 생성
                List<MatchUpEntity> nextRoundMatches = matchUpCreator.createNextRoundMatches(matchId, winnersImageIds);
                List<MatchUpEntity> savedNextRoundMatches = matchUpRepository.saveAll(nextRoundMatches);
                nextMatchId = savedNextRoundMatches.get(0).getId();
            }
        } else {
            // 이번 라운드 내의 다음 투표 경기 탐색
            MatchUpEntity nextMatchUp = freshMatchUps.stream()
                    .filter(m -> m.getRoundNumber() == currentRound && !m.isVoted())
                    .findFirst()
                    .orElse(null);

            if (nextMatchUp != null) {
                nextMatchId = nextMatchUp.getId();
            }
        }

        return new VoteSaveResponse(nextMatchId, currentRound, currentRoundOrder, isTournamentFinished);
    }
}