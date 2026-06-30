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

    private final MatchChoiceCreator matchChoiceCreator;
    private final MatchUpRepository matchUpRepository;
    private final MatchUpCreator matchUpCreator;

    @Transactional
    public VoteSaveResponse processVote(Long matchId, Long userId, VoteCommand command) {
        // 1. 현재 투표 중인 경기 조회 (낙관적 락 자동 적용)
        MatchUpEntity currentMatchUp = matchUpRepository.findById(command.getMatchUpId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (!currentMatchUp.getMatchId().equals(matchId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        // 2. 투표 데이터 기록 및 승자 업데이트
        currentMatchUp.validateCandidate(command.getPhotoId());
        currentMatchUp.updateWinner(command.getPhotoId());

        matchChoiceCreator.createChoice(currentMatchUp.getId(), command.getPhotoId(), command.getReasonId());

        // 3. 락 버전 갱신 및 플러시
        matchUpRepository.saveAndFlush(currentMatchUp);

        // 4. 라운드 전환 검사 및 다음 경기 반환
        return handleRoundTransition(matchId, currentMatchUp.getRoundNumber());
    }

    /**
     * 라운드 전환 로직: 대진표 생성 및 다음 매치 ID 계산
     */
    @Transactional
    public VoteSaveResponse handleRoundTransition(Long matchId, int currentRound) {
        // 전체 매치업을 최신 상태로 가져와서 계산
        List<MatchUpEntity> allMatchUps = matchUpRepository.findByMatchId(matchId);

        // 현재 라운드 진행 상황 파악
        List<MatchUpEntity> roundMatches = allMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound)
                .toList();

        int currentRoundOrder = (int) roundMatches.stream().filter(MatchUpEntity::isVoted).count();
        boolean isRoundCompleted = roundMatches.stream().allMatch(MatchUpEntity::isVoted);

        Long nextMatchId = null;
        boolean isTournamentFinished = false;

        if (isRoundCompleted) {
            // [Double-Checked Locking] 이미 다음 라운드가 생성되었는지 재확인
            boolean isNextRoundExists = allMatchUps.stream().anyMatch(m -> m.getRoundNumber() == currentRound + 1);

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
                    // 다음 라운드 대진표 생성
                    List<MatchUpEntity> nextRoundMatches = matchUpCreator.createNextRoundMatches(matchId, currentRound, winners);
                    nextMatchId = matchUpRepository.saveAll(nextRoundMatches).get(0).getId();
                }
            }
        } else {
            // 같은 라운드 내 다음 경기
            nextMatchId = roundMatches.stream()
                    .filter(m -> m.getState() == MatchUpState.NEED_VOTE)
                    .min(Comparator.comparing(MatchUpEntity::getId))
                    .map(MatchUpEntity::getId)
                    .orElse(null);
        }

        return new VoteSaveResponse(nextMatchId, currentRound, currentRoundOrder, isTournamentFinished);
    }

    /**
     * 토너먼트 전체에서 투표가 필요한 가장 빠른 매치업 ID를 탐색 (라운드 전환 대응)
     */
    private Long findNextNeedVoteMatchId(List<MatchUpEntity> allMatchUps) {
        return allMatchUps.stream()
                .filter(m -> m.getState() == MatchUpState.NEED_VOTE)
                .min(Comparator.comparing(MatchUpEntity::getRoundNumber)
                        .thenComparing(MatchUpEntity::getId))
                .map(MatchUpEntity::getId)
                .orElse(null);
    }
}