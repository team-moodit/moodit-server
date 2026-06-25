package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MatchUpCreator {

    public List<MatchUpEntity> createMatches(Long matchId, List<Long> imageIds) {
        // [팀 규칙] 에러 처리: imageIds가 null이거나 개수 범위(8~32)를 벗어날 경우
        if (imageIds == null || imageIds.size() < 8 || imageIds.size() > 32) {
            throw new ApiException(ErrorType.INVALID_IMAGE_COUNT);
        }

        int totalImages = imageIds.size();

        // 1. [수정] 피드백을 반영한 타겟 라운드 계산
        int targetRound = calculateTargetRound(totalImages);

        // 2. 경기 수 계산 (이제 음수가 나오지 않고 안전하게 양수로 계산됩니다.)
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

    /**
     *  [품질 개선] 15장, 31장 등의 입력에서 발생하던 IndexOutOfBoundsException 버그 해결
     * n보다 작은 '가장 큰 2의 거듭제곱' 단계를 구하여 부전승 대진을 완성하기 위한 핵심 연산입니다.
     */
    private int calculateTargetRound(int n) {
        if (n <= 0) return 0;
        // Integer.highestOneBit(n - 1)을 사용하면 15 입력 시 8, 31 입력 시 16을 정확하게 반환합니다.
        // 만약 딱 16이 들어오면 (16-1=15)의 highest bit인 8을 반환하여 8개 매치(16명 예선)를 치르게 유도합니다.
        return Integer.highestOneBit(n - 1);
    }
}