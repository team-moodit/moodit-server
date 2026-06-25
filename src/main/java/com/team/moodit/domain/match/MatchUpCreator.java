package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType; // 📌 정의해주신 ErrorType 임포트
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MatchUpCreator {

    public List<MatchUpEntity> createMatches(Long matchId, List<Long> imageIds) {
        // [팀 규칙] 에러 처리: imageIds가 null이거나 개수 범위(8~32)를 벗어날 경우
        if (imageIds == null || imageIds.size() < 8 || imageIds.size() > 32) {
            throw new ApiException(ErrorType.INVALID_IMAGE_COUNT); // 📌 정의된 에러 타입 사용
        }

        int totalImages = imageIds.size();

        // 1. 타겟 라운드 계산 (기획서 v0.3 반영 로직)
        int targetRound = calculateTargetRound(totalImages);

        // 2. 경기 수 계산
        int matchCount = totalImages - targetRound;
        int firstRoundPlayersCount = matchCount * 2;

        List<Long> shuffledIds = new ArrayList<>(imageIds);
        Collections.shuffle(shuffledIds);

        List<MatchUpEntity> matchUps = new ArrayList<>();

        // 3. 예선전 대결 매치업 등록 (NEED_VOTE)
        for (int i = 0; i < firstRoundPlayersCount; i += 2) {
            matchUps.add(MatchUpEntity.of(
                    new RealMatchUp(matchId, totalImages, shuffledIds.get(i), shuffledIds.get(i + 1))
            ));
        }

        // 4. 부전승 자동 진출 등록 (SKIPPED)
        for (int i = firstRoundPlayersCount; i < totalImages; i++) {
            matchUps.add(MatchUpEntity.of(
                    new AutoPassMatch(matchId, totalImages, shuffledIds.get(i))
            ));
        }

        return matchUps;
    }

    private int calculateTargetRound(int n) {
        int highestOneBit = Integer.highestOneBit(n);
        if (highestOneBit == n) {
            return n / 2;
        }
        return highestOneBit;
    }
}