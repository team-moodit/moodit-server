package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchUpEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MatchMaker {

    public List<MatchUpEntity> createMatches(Long matchId, List<Long> imageIds) {
        int totalImages = imageIds.size();

        // 8장 ~ 32장 제한 조건 방어 코드 추가
        if (totalImages < 8 || totalImages > 32) {
            throw new IllegalArgumentException("토너먼트는 최소 8장, 최대 32장의 이미지만 참여 가능합니다. (현재: " + totalImages + "장)");
        }

        // [수정된 로직] n 이하의 가장 큰 2의 거듭제곱 수 반환 (8, 16, 32 중 하나가 됨)
        int targetRound = Integer.highestOneBit(totalImages);

        int matchCount = totalImages - targetRound;
        int firstRoundPlayersCount = matchCount * 2;

        List<Long> shuffledIds = new ArrayList<>(imageIds);
        Collections.shuffle(shuffledIds);

        List<MatchUpEntity> matchUps = new ArrayList<>();

        // 예선전 등록 (NEED_VOTE)
        for (int i = 0; i < firstRoundPlayersCount; i += 2) {
            matchUps.add(MatchUpEntity.of(
                    new RealMatchUp(matchId, totalImages, shuffledIds.get(i), shuffledIds.get(i + 1))
            ));
        }

        // 부전승 등록 (SKIPPED)
        for (int i = firstRoundPlayersCount; i < totalImages; i++) {
            matchUps.add(MatchUpEntity.of(
                    new AutoPassMatch(matchId, totalImages, shuffledIds.get(i))
            ));
        }

        return matchUps;
    }
}
