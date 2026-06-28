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

        // 6. [보정] 부전승(SKIPPED) 경기를 제외한 '진짜 경기' 리스트만 필터링
        int currentRound = currentMatchUp.getRoundNumber();

        List<MatchUpEntity> actualMatchesInRound = freshMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound)
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        // 이번 라운드 누적 투표 완료 횟수
        int currentRoundOrder = (int) actualMatchesInRound.stream()
                .filter(MatchUpEntity::isVoted)
                .count();

        // 판단 기준: '진짜 경기'가 전부 투표 완료되었는가?
        boolean isRoundCompleted = actualMatchesInRound.stream()
                .allMatch(MatchUpEntity::isVoted);

        Long nextMatchId = null;
        boolean isTournamentFinished = false;

        // 7. 대진 흐름 제어 (라운드 종료 체크)
        if (isRoundCompleted) {
            // 다음 라운드로 진출할 승자 ID들을 순서대로 수집 (부전승 포함)
            List<Long> winnersImageIds = freshMatchUps.stream()
                    .filter(m -> m.getRoundNumber() == currentRound)
                    .map(MatchUpEntity::getWinnerId)
                    .filter(id -> id != null && id != 0L)
                    .toList();

            // 승자가 최종 1명뿐이라면 토너먼트 최종 종료
            if (winnersImageIds.size() == 1) {
                isTournamentFinished = true;
            } else {
                // 💡 변경: 현재 라운드 번호(currentRound)를 넘겨주어 순차 증가(1->2->3)하도록 제어
                List<MatchUpEntity> nextRoundMatches = matchUpCreator.createNextRoundMatches(matchId, currentRound, winnersImageIds);
                List<MatchUpEntity> savedNextRoundMatches = matchUpRepository.saveAll(nextRoundMatches);
                nextMatchId = savedNextRoundMatches.get(0).getId();
            }
        } else {
            // 이번 라운드 내에서 유저가 투표해야 하는 다음 진짜 경기를 탐색 (부전승 제외)
            MatchUpEntity nextMatchUp = actualMatchesInRound.stream()
                    .filter(m -> !m.isVoted())
                    .findFirst()
                    .orElse(null);

            if (nextMatchUp != null) {
                nextMatchId = nextMatchUp.getId();
            }
        }

        return new VoteSaveResponse(nextMatchId, currentRound, currentRoundOrder, isTournamentFinished);
    }
}