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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchUpReader {

    private final MatchRepository matchRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchVoteRepository matchVoteRepository;
    private final FileReader fileReader;



    public MatchUpStart getMatchUp(Long matchId) {
        // 1. 대진표(MatchUp) 목록을 외래키 matchId 기준으로 먼저 필터링하여 가져옵니다.
        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);
        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        // 현재 진행해야 하는 첫 번째 대진 획득
        MatchUpEntity matchUp = matchUps.get(0);

        // 2. 획득한 대진표 로우의 실제 matchId 값을 역으로 추적하여 정확하게 부모를 찾아옵니다.
        MatchEntity match = matchRepository.findById(matchUp.getMatchId())
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 3. 파일 인프라 연동 및 Null 예외 방어
        File fileA = fileReader.getFile(matchUp.getCandidateAId());
        File fileB = fileReader.getFile(matchUp.getCandidateBId());

        if (fileA == null || fileB == null) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        // 4. match_vote 4개 셔플 및 안전 샘플링
        List<MatchVoteEntity> allVotes = matchVoteRepository.findAll();
        List<MatchVoteEntity> sampledVotes;

        if (allVotes == null || allVotes.isEmpty()) {
            sampledVotes = List.of();
        } else {
            List<MatchVoteEntity> shuffleTarget = new ArrayList<>(allVotes);
            Collections.shuffle(shuffleTarget);
            sampledVotes = shuffleTarget.stream().limit(4).toList();
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