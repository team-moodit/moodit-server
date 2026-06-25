package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchVoteEntity;
import com.team.moodit.storage.db.core.MatchVoteRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.file.FileReader;
import com.team.moodit.support.file.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchUpReader {

    private final MatchRepository matchRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchVoteRepository matchVoteRepository;
    private final FileReader fileReader;

    public MatchUpStart getMatchUp(Long matchId) {
        // 1. [개선] 파라미터로 받은 matchId를 사용해 부모 매치를 바로 조회합니다. (역방향 제거)
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 2. 해당 매치에 속한 대진표(MatchUp) 목록을 정렬하여 가져옵니다.
        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);
        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        // 정렬 조건이 보장된 첫 번째 대진 획득
        MatchUpEntity matchUp = matchUps.get(0);

        // 3. 파일 인프라 연동 및 Null 예외 방어
        File fileA = fileReader.getFile(matchUp.getCandidateAId());
        File fileB = fileReader.getFile(matchUp.getCandidateBId());

        if (fileA == null || fileB == null) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        // 4. DB 레벨에서 딱 4개의 랜덤 데이터만 효율적으로 조회
        List<MatchVoteEntity> sampledVotes = matchVoteRepository.findRandomVotes();
        if (sampledVotes == null) {
            sampledVotes = List.of();
        }

        // 5. 총 이미지 수 기반 강수 연산
        int totalImages = match.getInitialImageCount();
        int totalRounds = (int) Math.ceil(Math.log(totalImages) / Math.log(2));

        String roundName = totalImages + "강전";
        if (totalImages == 2) roundName = "결승전";
        else if (totalImages == 4) roundName = "준결승전";

        // 6. 도메인 객체 MatchUpStart 조립 반환
        return new MatchUpStart(
                match.getTitle(),
                totalRounds,
                matchUp.getRoundNumber(),
                roundName,
                fileA.getId(),
                fileA.getUrl(),
                fileB.getId(),
                fileB.getUrl(),
                sampledVotes
        );
    }
}