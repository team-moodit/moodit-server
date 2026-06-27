package com.team.moodit.domain.match;

import com.team.moodit.api.controller.v1.response.MatchStartResponse;
import com.team.moodit.api.controller.v1.response.MatchUpFlowResponse;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchVoteRepository;
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
    private final MatchVoteRepository matchVoteRepository;
    private final FileReader fileReader; // 추가

    @Transactional(readOnly = true)
    public MatchUpFlowResponse findNextMatchUp(Long matchId) {

        // 1. MatchEntity에서 진짜 등록된 타이틀 동적 조회
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 2. 대진표 라인업 전체 조회
        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);
        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        // 3. 아직 투표 안 한 진행 예정 매치업 확보
        MatchUpEntity nextTarget = matchUps.stream()
                .filter(m -> !m.isVoted())
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_REQUEST));

        // 4. 부전승 공백 경기 스킵 방어막
        if (nextTarget.getCandidateBId() == null || nextTarget.getCandidateBId() == 0L) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        // 5. 현재 라운드 매치 인덱스 계산 (예: 4경기 중 2번째 경기)
        List<MatchUpEntity> sameRoundMatchUps = matchUps.stream()
                .filter(m -> m.getRoundNumber() == nextTarget.getRoundNumber())
                .toList();

        int totalMatchUpInRound = sameRoundMatchUps.size();
        long completedCount = sameRoundMatchUps.stream().filter(MatchUpEntity::isVoted).count();
        int currentMatchIndex = (int) completedCount + 1;

        // 6. MatchVote 테이블에서 이유 보기 목록 조회
        List<MatchStartResponse.ReasonResponse> reasons = matchVoteRepository.findAll().stream()
                .map(v -> new MatchStartResponse.ReasonResponse(
                        v.getId(),
                        v.getContent()
                ))
                .toList();

        // 7. candidate_id = file_id 이므로 FileReader로 URL 조회
        String candidateAUrl = fileReader.getFile(nextTarget.getCandidateAId()).getUrl();
        String candidateBUrl = fileReader.getFile(nextTarget.getCandidateBId()).getUrl();

        // 8. 최종 결과 반환
        return new MatchUpFlowResponse(
                match.getTitle(),
                nextTarget.getRoundNumber() + "강전",
                currentMatchIndex,
                totalMatchUpInRound,
                false,
                new MatchStartResponse.NextMatchUpResponse(
                        new MatchStartResponse.CandidateResponse(nextTarget.getCandidateAId(), candidateAUrl),
                        new MatchStartResponse.CandidateResponse(nextTarget.getCandidateBId(), candidateBUrl)
                ),
                reasons
        );
    }
}