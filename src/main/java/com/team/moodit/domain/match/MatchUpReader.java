package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import com.team.moodit.storage.db.core.MatchVoteCandidateRepository; // 📌 추가 필요
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.file.FileReader;
import com.team.moodit.support.file.File;
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
    private final MatchVoteCandidateRepository matchVoteCandidateRepository; // 📌 레포지토리 주입 추가
    private final FileReader fileReader;

    @Transactional(readOnly = true)
    public MatchUpStart getMatchUp(Long matchId) {
        // 1. 부모 매치 조회
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 2. 현재 투표 진행 중인 매치업(ING) 획득
        Optional<MatchUpEntity> currentMatchUpOpt = matchUpRepository.findFirstByMatchIdAndState(matchId, MatchUpState.NEED_VOTE);

        // 더 이상 진행할 투표(ING)가 없다면 최종 종료 처리
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

        // 현재 유저가 진행해야 하는 '실질 투표 차례' (완료된 경기 수 + 1)
        int currentRound = matchUpRepository.countByMatchIdAndState(matchId, MatchUpState.COMPLETED) + 1;

        // 4.  [수정 필수] Creator가 해당 라운드에 저장해 둔 사유 4개를 순서대로 조회
        // (조회 시 DB에 저장된 순서인 r1 -> r2 -> r3 -> r4가 깨지지 않도록 id 오름차순 정렬 정합성 등을 보장하는 쿼리 사용 추천)
        List<MatchVoteCandidateEntity> sampledVotes = matchVoteCandidateRepository.findAllByMatchIdAndRoundNumberOrderByIdAsc(matchId, currentRound);
        if (sampledVotes == null) {
            sampledVotes = List.of();
        }

        // 5.  Creator의 totalMatchRounds 공식(이미지수 - 1)과 100% 일치시켜 정합성 확보
        int totalImages = match.getInitialImageCount();
        int totalRounds = totalImages - 1;

        // 6. 강수 이름 판별 (2의 거듭제곱이 아니면 예선전)
        String roundName;
        int currentMatchPlayers = matchUp.getRoundNumber();
        boolean isPowerOfTwo = (currentMatchPlayers > 0) && ((currentMatchPlayers & (currentMatchPlayers - 1)) == 0);

        if (!isPowerOfTwo) {
            roundName = "예선전";
        } else if (currentMatchPlayers == 2) {
            roundName = "결승전";
        } else if (currentMatchPlayers == 4) {
            roundName = "준결승전";
        } else {
            roundName = currentMatchPlayers + "강전"; // 8강전, 16강전 등
        }

        boolean isTournamentCompleted = false;

        return new MatchUpStart(
                match.getTitle(),
                totalRounds,           // 9장일 때 8, 16장일 때 15, 24장일 때 23 고정
                currentRound,          // 1부터 totalRounds까지 정직하게 누적 증가
                roundName,
                isTournamentCompleted,
                fileA.getId(),
                fileA.getUrl(),
                fileB.getId(),
                fileB.getUrl(),
                sampledVotes           //  정렬된 채 꽂힌 4개의 세트 데이터 반환
        );
    }
}