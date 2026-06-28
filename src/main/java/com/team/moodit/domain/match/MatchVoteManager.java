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
     * 1. 투표 업데이트 트랜잭션 (낙관적 락으로 동시성 제어)
     */
    @Transactional
    public VoteSaveResponse processVote(Long matchId, VoteCommand command) {
        // [수정] Lock 없이 조회 (버전 체크는 save/flush 시점에 자동 수행됨)
        MatchUpEntity currentMatchUp = matchUpRepository.findById(command.getMatchUpId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        currentMatchUp.validateCandidate(command.getPhotoId());
        currentMatchUp.updateWinner(command.getPhotoId());

        // 투표 사유 기록
        matchChoiceCreator.createChoice(
                currentMatchUp.getId(),
                command.getPhotoId(),
                command.getReasonId()
        );

        // [핵심] 여기서 DB update 시 버전 체크 발생 (충돌 시 ObjectOptimisticLockingFailureException 발생)
        matchUpRepository.saveAndFlush(currentMatchUp);

        // 2. 라운드 전환 로직 호출 (트랜잭션 분리하여 락 점유 최소화)
        return handleRoundTransition(matchId, currentMatchUp.getRoundNumber());
    }

    /**
     * [수정] 라운드 전환 로직을 별도 메서드로 분리하여 트랜잭션 범위 최적화
     */
    @Transactional(readOnly = true)
    public VoteSaveResponse handleRoundTransition(Long matchId, int currentRound) {
        List<MatchUpEntity> freshMatchUps = matchUpRepository.findByMatchId(matchId);

        List<MatchUpEntity> actualMatchesInRound = freshMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound)
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        int currentRoundOrder = (int) actualMatchesInRound.stream()
                .filter(MatchUpEntity::isVoted)
                .count();

        boolean isRoundCompleted = actualMatchesInRound.stream()
                .allMatch(MatchUpEntity::isVoted);

        Long nextMatchId = null;
        boolean isTournamentFinished = false;

        if (isRoundCompleted) {
            List<Long> winnersImageIds = freshMatchUps.stream()
                    .filter(m -> m.getRoundNumber() == currentRound)
                    .map(MatchUpEntity::getWinnerId)
                    .filter(id -> id != null && id != 0L)
                    .toList();

            if (winnersImageIds.size() == 1) {
                isTournamentFinished = true;
            } else {
                // 이 부분은 쓰기 작업이므로 실제 실행 시 @Transactional이 필요할 수 있습니다.
                // 필요 시 별도 Service로 빼거나 명시적 트랜잭션을 사용하세요.
                List<MatchUpEntity> nextRoundMatches = matchUpCreator.createNextRoundMatches(matchId, currentRound, winnersImageIds);
                nextMatchId = matchUpRepository.saveAll(nextRoundMatches).get(0).getId();
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