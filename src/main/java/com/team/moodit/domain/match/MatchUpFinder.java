package com.team.moodit.domain.match;

import com.team.moodit.api.controller.v1.response.MatchStartResponse;
import com.team.moodit.api.controller.v1.response.MatchUpFlowResponse;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchEntity;
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

        // =========================================================================
        // 5. 🎯 [핵심 변경] 보기(Reasons) 매핑용 글로벌 경기 인덱스 계산 (Global Match Index)
        // =========================================================================
        // 전체 대진표 중에서 부전승이 아닌 '진짜 유저가 손으로 치러야 하는 경기들'만 필터링
        List<MatchUpEntity> allActualMatches = matchUps.stream()
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        // 토너먼트 전체를 통틀어 이미 투표 완료된 총 경기 수 카운트
        long totalCompletedCount = allActualMatches.stream()
                .filter(MatchUpEntity::isVoted)
                .count();

        // 💡 현재 경기가 토너먼트 전체에서 '몇 번째 진짜 경기'인지 계산하여 보기 테이블의 round_number와 매칭
        int currentMatchIndex = (int) totalCompletedCount + 1;


        // =========================================================================
        // 6. [타이틀 보정] 라운드 번호와 진짜 경기 수를 조합해 완벽하게 타이틀 판별
        // =========================================================================
        // 현재 유저가 진행 중인 '해당 라운드'의 경기들만 필터링
        List<MatchUpEntity> sameRoundMatchUps = matchUps.stream()
                .filter(m -> m.getRoundNumber() == nextTarget.getRoundNumber())
                .toList();

        // 해당 라운드 내에서 부전승을 제외한 진짜 경기들만 필터링
        List<MatchUpEntity> actualMatchesInRound = sameRoundMatchUps.stream()
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        int totalMatchUpInRound = actualMatchesInRound.size(); // 이번 라운드에 유저가 치러야 할 총 경기 수
        long completedCountInRound = actualMatchesInRound.stream().filter(MatchUpEntity::isVoted).count();
        int displayMatchIndex = (int) completedCountInRound + 1; // 화면에 보여줄 경기 인덱스 (예: 1 / 4 경기)

        String roundTitle;
        int targetRoundNumber = nextTarget.getRoundNumber(); // 💡 1, 2, 3... 순차 증가하는 라운드 번호

        if (targetRoundNumber == 1) {
            // 💡 [최우선 가드] 1라운드는 진짜 경기 수가 몇 개든 묻고 따지지도 않고 무조건 "예선전"입니다.
            roundTitle = "예선전";
        } else if (totalMatchUpInRound == 1) {
            // 💡 2라운드 이상이면서 진짜 남은 경기가 1개라면 대망의 "결승전"입니다.
            roundTitle = "결승전";
        } else if (totalMatchUpInRound == 2) {
            roundTitle = "준결승전";
        } else if (totalMatchUpInRound == 4) {
            roundTitle = "8강전"; // 🎯 이제 2라운드에 진짜 경기 4개인 상태이므로 이곳에 정확히 걸립니다!
        } else if (totalMatchUpInRound == 8) {
            roundTitle = "16강전";
        } else if (totalMatchUpInRound == 16) {
            roundTitle = "32강전";
        } else {
            roundTitle = "본선";
        }

        // =========================================================================
        // 7.  보정된 currentMatchIndex(글로벌 경기 번호)를 사용하여 정확한 보기 데이터 조회
        // =========================================================================
        List<MatchStartResponse.ReasonResponse> reasons = matchVoteCandidateRepository
                .findAllByMatchIdAndRoundNumberOrderByIdAsc(matchId, currentMatchIndex).stream()
                .map(v -> new MatchStartResponse.ReasonResponse(
                        v.getId(),
                        v.getContent()
                ))
                .toList();

        // 8. candidate_id = file_id 이므로 FileReader로 URL 조회
        String candidateAUrl = fileReader.getFile(nextTarget.getCandidateAId()).getUrl();
        String candidateBUrl = fileReader.getFile(nextTarget.getCandidateBId()).getUrl();

        // 9. 최종 결과 반환 (displayMatchIndex를 넘겨주어 화면에 현재 라운드 기준 진행도가 나오게 함)
        return new MatchUpFlowResponse(
                match.getTitle(),
                roundTitle,
                displayMatchIndex,
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