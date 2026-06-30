package com.team.moodit.domain.match;

import com.team.moodit.api.controller.v1.response.MatchStartResponse;
import com.team.moodit.api.controller.v1.response.MatchUpFlowResponse;
import com.team.moodit.storage.db.core.*;
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

        // 1. 투표 완료 여부 판별 (전체 투표 개수 확인)
        long totalMatches = (matchUps == null) ? 0 : matchUps.size();
        long votedMatches = (matchUps == null) ? 0 : matchUps.stream().filter(MatchUpEntity::isVoted).count();
        boolean isTournamentCompleted = (totalMatches > 0 && votedMatches >= totalMatches);

        // 2. [완료 상태인 경우] 즉시 리턴 (불필요한 라운드 로직 진입 방지)
        if (isTournamentCompleted) {
            List<MatchVoteCandidateEntity> allReasons = matchVoteCandidateRepository.findAllByMatchId(matchId);

            List<MatchStartResponse.ReasonResponse> reasons = (allReasons == null ? List.<MatchVoteCandidateEntity>of() : allReasons)
                    .stream()
                    .map(v -> new MatchStartResponse.ReasonResponse(v.getId(), v.getContent())) // 이제 v가 MatchVoteCandidateEntity로 인식됨
                    .toList();
            return new MatchUpFlowResponse(match.getTitle(), "결승전", 0, 0, true, null, reasons);
        }

        // 3. [진행 중인 경우] 다음 타겟 탐색
        MatchUpEntity nextTarget = matchUps.stream()
                .filter(m -> !m.isVoted())
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 4. 라운드 정보 및 인덱스 계산
        List<MatchUpEntity> actualMatches = matchUps.stream()
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        List<MatchUpEntity> sameRoundMatchUps = matchUps.stream()
                .filter(m -> m.getRoundNumber() == nextTarget.getRoundNumber())
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        int totalMatchUpInRound = sameRoundMatchUps.size();
        int completedCountInRound = (int) sameRoundMatchUps.stream().filter(MatchUpEntity::isVoted).count();
        int displayMatchIndex = completedCountInRound + 1;
        int currentMatchIndex = (int) actualMatches.stream().filter(MatchUpEntity::isVoted).count() + 1;

        // 라운드 타이틀 결정
        String roundTitle = getRoundTitle(nextTarget.getRoundNumber(), totalMatchUpInRound, match.getInitialImageCount());

        // 다음 매치업 응답 구성
        String candidateAUrl = fileReader.getFile(nextTarget.getCandidateAId()).getUrl();
        String candidateBUrl = fileReader.getFile(nextTarget.getCandidateBId()).getUrl();
        MatchStartResponse.NextMatchUpResponse nextMatchUpResponse = new MatchStartResponse.NextMatchUpResponse(
                nextTarget.getId(),
                new MatchStartResponse.CandidateResponse(nextTarget.getCandidateAId(), candidateAUrl),
                new MatchStartResponse.CandidateResponse(nextTarget.getCandidateBId(), candidateBUrl)
        );

        // 사유 데이터 조회
        List<MatchVoteCandidateEntity> sourceList = matchVoteCandidateRepository
                .findAllByMatchIdAndRoundNumberOrderByIdAsc(matchId, nextTarget.getRoundNumber());

        List<MatchStartResponse.ReasonResponse> reasons = (sourceList == null ? List.<MatchVoteCandidateEntity>of() : sourceList).stream()
                .map(v -> new MatchStartResponse.ReasonResponse(v.getId(), v.getContent()))
                .toList();

        return new MatchUpFlowResponse(
                match.getTitle(), roundTitle, displayMatchIndex, totalMatchUpInRound, false, nextMatchUpResponse, reasons
        );
    }

    private String getRoundTitle(int roundNumber, int totalMatchUp, int totalImages) {
        boolean isPerfectBracket = (totalImages == 4 || totalImages == 8 || totalImages == 16 || totalImages == 32);
        if (roundNumber == 1 && !isPerfectBracket) return "예선전";
        if (totalMatchUp == 1) return "결승전";
        if (totalMatchUp == 2) return "준결승전";
        if (totalMatchUp == 4) return "8강전";
        if (totalMatchUp == 8) return "16강전";
        if (totalMatchUp == 16) return "32강전";
        return "결승전";
    }
}