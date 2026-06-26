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

        //  [수정 완료] 데이터 조회 시점에 비관적 락을 걸어 동시성 요청을 한 줄로 세운다.
        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchIdWithLock(matchId);
        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        // 2. 현재 투표해야 하는 진행 중인 대진(미투표 경기) 탐색
        MatchUpEntity currentMatchUp = matchUps.stream()
                .filter(m -> !m.isVoted())
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_REQUEST));

        // 3. 유저가 선택한 사진이 이번 대진의 후보가 맞는지 비즈니스 검증
        currentMatchUp.validateCandidate(command.getPhotoId());

        // 4. 승자 확정 및 엔티티 상태 변경
        currentMatchUp.updateWinner(command.getPhotoId());

        // 📌 [PostgreSQL & 영속성 동기화 최적화]
        // 현재 경기 투표 상태를 디비에 즉시 완전히 반영하여 확정 짓습니다.
        matchUpRepository.saveAndFlush(currentMatchUp);

        // 5. 유저가 선택한 투표 이유 영수증(Choice) 기록 저장
        matchChoiceCreator.createChoice(
                currentMatchUp.getId(),
                command.getPhotoId(),
                command.getReasonId()
        );

        // 📌 [핵심 수정 포인트] 방금 저장된 상태를 기준으로 디비에서 최신 대진표 리스트를 깔끔하게 다시 불러옵니다!
        List<MatchUpEntity> freshMatchUps = matchUpRepository.findByMatchId(matchId);

        // 6. 메타데이터 계산 및 대진 흐름 제어 진입 (최신화된 freshMatchUps 리스트 사용)
        int currentRound = currentMatchUp.getRoundNumber();

        int currentRoundOrder = (int) freshMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound && m.isVoted())
                .count();

        boolean isRoundCompleted = freshMatchUps.stream()
                .filter(m -> m.getRoundNumber() == currentRound)
                .allMatch(MatchUpEntity::isVoted);

        Long nextMatchId = null;
        boolean isTournamentFinished = false;

        if (isRoundCompleted) {
            if (currentRound == 2) {
                isTournamentFinished = true;
            } else {
                // 상위 라운드로 진출할 진짜 확정된 승자 사진 ID 리스트 추출
                List<Long> winnersImageIds = freshMatchUps.stream()
                        .filter(m -> m.getRoundNumber() == currentRound)
                        .map(MatchUpEntity::getWinnerId)
                        .toList();

                //  기존의 createMatches 대신, 4강/결승 전용인 createNextRoundMatches를 호출합니다!
                List<MatchUpEntity> nextRoundMatches = matchUpCreator.createNextRoundMatches(matchId, winnersImageIds);

                // 생성된 차기 라운드 매치업들을 저장하고 첫 번째 경기 ID를 획득합니다.
                List<MatchUpEntity> savedNextRoundMatches = matchUpRepository.saveAll(nextRoundMatches);
                nextMatchId = savedNextRoundMatches.get(0).getId();
            }
        } else {
            // 남은 미투표 대진 중 다음 타겟 탐색
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