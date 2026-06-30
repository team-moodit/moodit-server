package com.team.moodit.domain.match;

import com.team.moodit.api.controller.v1.response.MatchStartResponse;
import com.team.moodit.api.controller.v1.response.MatchUpFlowResponse;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
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

        // 아직 투표를 진행하지 않은 다음 매치업 탐색
        MatchUpEntity nextTarget = matchUps.stream()
                .filter(m -> !m.isVoted())
                .findFirst()
                .orElse(null);

        // 진행할 타겟 경기가 없으면 토너먼트 전체 완료로 판단
        boolean isTournamentCompleted = (nextTarget == null);

        String roundTitle = "결승전";
        int displayMatchIndex = 1;
        int totalMatchUpInRound = 1;
        int currentMatchIndex = 1;
        MatchStartResponse.NextMatchUpResponse nextMatchUpResponse = null;

        List<MatchUpEntity> allActualMatches = matchUps.stream()
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        long totalCompletedCount = allActualMatches.stream()
                .filter(MatchUpEntity::isVoted)
                .count();

        if (isTournamentCompleted) {
            currentMatchIndex = (int) totalCompletedCount;
        } else {
            if (nextTarget.getCandidateBId() == null || nextTarget.getCandidateBId() == 0L) {
                throw new ApiException(ErrorType.INVALID_REQUEST);
            }

            currentMatchIndex = (int) totalCompletedCount + 1;

            List<MatchUpEntity> sameRoundMatchUps = matchUps.stream()
                    .filter(m -> m.getRoundNumber() == nextTarget.getRoundNumber())
                    .toList();

            List<MatchUpEntity> actualMatchesInRound = sameRoundMatchUps.stream()
                    .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                    .toList();

            totalMatchUpInRound = actualMatchesInRound.size();
            long completedCountInRound = actualMatchesInRound.stream().filter(MatchUpEntity::isVoted).count();
            displayMatchIndex = (int) completedCountInRound + 1;

            int targetRoundNumber = nextTarget.getRoundNumber();
            int totalImages = match.getInitialImageCount();
            boolean isPerfectBracket = (totalImages == 4 || totalImages == 8 || totalImages == 16 || totalImages == 32);

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

            String candidateAUrl = fileReader.getFile(nextTarget.getCandidateAId()).getUrl();
            String candidateBUrl = fileReader.getFile(nextTarget.getCandidateBId()).getUrl();

            nextMatchUpResponse = new MatchStartResponse.NextMatchUpResponse(
                    nextTarget.getId(),
                    new MatchStartResponse.CandidateResponse(nextTarget.getCandidateAId(), candidateAUrl),
                    new MatchStartResponse.CandidateResponse(nextTarget.getCandidateBId(), candidateBUrl)
            );
        }

        // 동적 대진표(부전승 포함) 대응을 위한 실제 DB 기반 최대 라운드 연산
        int maxRoundNumber = matchUps.stream()
                .mapToInt(MatchUpEntity::getRoundNumber)
                .max()
                .orElse(1);

        int queryRoundNumber = isTournamentCompleted ? maxRoundNumber : nextTarget.getRoundNumber();

        // 토너먼트 종료 시점에는 결승전을 포함한 전체 라운드 표(사유 데이터)를 취합하고, 진행 중일 때는 해당 라운드 데이터만 조회
        List<MatchVoteCandidateEntity> sourceList;
        if (isTournamentCompleted) {
            sourceList = matchVoteCandidateRepository.findAllByMatchId(matchId);
        } else {
            sourceList = matchVoteCandidateRepository.findAllByMatchIdAndRoundNumberOrderByIdAsc(matchId, queryRoundNumber);
        }

        List<MatchStartResponse.ReasonResponse> reasons = sourceList.stream()
                .map(v -> new MatchStartResponse.ReasonResponse(v.getId(), v.getContent()))
                .toList();

        return new MatchUpFlowResponse(
                match.getTitle(),
                roundTitle,
                displayMatchIndex,
                totalMatchUpInRound,
                isTournamentCompleted,
                nextMatchUpResponse,
                reasons
        );
    }
}