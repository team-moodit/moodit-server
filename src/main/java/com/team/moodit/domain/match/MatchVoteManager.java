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
     * 투표 처리 (낙관적 락 적용)
     */
    @Transactional
    public VoteSaveResponse processVote(Long matchId, VoteCommand command) {
        // 1. 특정 경기 ID로 조회 (낙관적 락이 버전 체크 수행)
        MatchUpEntity currentMatchUp = matchUpRepository.findById(command.getMatchUpId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 2. 검증: 요청받은 경기가 해당 토너먼트 소속이 맞는지 확인
        if (!currentMatchUp.getMatchId().equals(matchId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        // 3. 비즈니스 검증 및 승자 확정
        currentMatchUp.validateCandidate(command.getPhotoId());
        currentMatchUp.updateWinner(command.getPhotoId());

        // 투표 기록 저장
        matchChoiceCreator.createChoice(
                currentMatchUp.getId(),
                command.getPhotoId(),
                command.getReasonId()
        );

        // 여기서 버전 체크 발생 (동시 투표 시 충돌 시점에 Exception 발생)
        matchUpRepository.saveAndFlush(currentMatchUp);

        // 4. 라운드 전환 로직은 트랜잭션을 분리하여 수행
        return handleRoundTransition(matchId, currentMatchUp.getRoundNumber());
    }

    /**
     * 라운드 전환 및 다음 경기 계산 (읽기/쓰기 복합 작업)
     */
    @Transactional
    public VoteSaveResponse handleRoundTransition(Long matchId, int currentRound) {
        List<MatchUpEntity> freshMatchUps = matchUpRepository.findByMatchId(matchId);

        List<MatchUpEntity> actualMatchesInRound = freshMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound)
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        if (actualMatchesInRound.isEmpty()) {
            // 이미 라운드가 종료되었거나 데이터 정합성이 깨진 경우를 대비
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