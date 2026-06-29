package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import com.team.moodit.storage.db.core.MatchVoteCandidateRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.file.File;
import com.team.moodit.support.file.FileReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MatchUpReader {

    private final MatchRepository matchRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchVoteCandidateRepository matchVoteCandidateRepository;
    private final FileReader fileReader;

    @Transactional(readOnly = true)
    public MatchUpStart getMatchUp(Long matchId) {
        // 1. 부모 매치 조회
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 2. 현재 투표 진행 중인 매치업(NEED_VOTE) 획득
        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);
        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        Optional<MatchUpEntity> currentMatchUpOpt = matchUps.stream()
                .filter(m -> m.getState() == MatchUpState.NEED_VOTE)
                .findFirst();

        // 더 이상 진행할 투표가 없다면 최종 종료 처리
        if (currentMatchUpOpt.isEmpty()) {
            return MatchUpStart.createCompleted(match.getTitle());
        }

        MatchUpEntity matchUp = currentMatchUpOpt.get();

        // 3. 파일 인프라 연동
        File fileA = fileReader.getFile(matchUp.getCandidateAId());
        File fileB = fileReader.getFile(matchUp.getCandidateBId());

        if (fileA == null || fileB == null) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        // =========================================================================
        //  [정합성 보정] 유저가 '진짜 투표 완료한 총 경기 수'를 기준으로 글로벌 인덱스 계산
        // =========================================================================
        long totalCompletedCount = matchUps.stream()
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .filter(m -> m.getState() == MatchUpState.COMPLETED || m.isVoted())
                .count();

        int currentRound = (int) totalCompletedCount + 1; // 보기 매핑용 인덱스

        // 4. Creator가 해당 순서에 저장해 둔 사유 4개 조회
        List<MatchVoteCandidateEntity> sampledVotes = matchVoteCandidateRepository
                .findAllByMatchIdAndRoundNumberOrderByIdAsc(matchId, currentRound);
        if (sampledVotes == null) {
            sampledVotes = List.of();
        }

        // 5. 총 라운드 수 공식
        int totalImages = match.getInitialImageCount();
        int totalRounds = totalImages - 1;

        // =========================================================================
        //  [타이틀 보정] 라운드 번호와 진짜 경기 수를 조합해 완벽하게 타이틀 판별
        // =========================================================================
        int targetRoundNumber = matchUp.getRoundNumber(); // 1, 2, 3... 순차 증가된 라운드 번호

        List<MatchUpEntity> actualMatchesInRound = matchUps.stream()
                .filter(m -> m.getRoundNumber() == targetRoundNumber)
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        int totalMatchUpInRound = actualMatchesInRound.size(); // 이번 라운드의 진짜 경기 수
        String roundName;

        if (targetRoundNumber == 1) {
            //  [최우선 가드] 1라운드는 진짜 경기 수가 몇 개 남았든 무조건 "예선전"으로 분류합니다.
            roundName = "예선전";
        } else if (totalMatchUpInRound == 1) {
            //  2라운드 이상이면서 진짜 경기가 딱 1개 남은 순간이 진짜 "결승전"입니다.
            roundName = "결승전";
        } else if (totalMatchUpInRound == 2) {
            roundName = "준결승전"; // 4강전
        } else if (totalMatchUpInRound == 4) {
            roundName = "8강전";
        } else if (totalMatchUpInRound == 8) {
            roundName = "16강전";
        } else if (totalMatchUpInRound == 16) {
            roundName = "32강전";
        } else {
            roundName = "본선";
        }

        boolean isTournamentCompleted = false;

        return new MatchUpStart(
                matchUp.getId(),
                match.getTitle(),
                totalRounds,
                currentRound,
                roundName,
                isTournamentCompleted,
                fileA.getId(),
                fileA.getUrl(),
                fileB.getId(),
                fileB.getUrl(),
                sampledVotes
        );
    }
}