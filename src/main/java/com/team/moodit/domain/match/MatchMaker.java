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

        // 1. 타겟 라운드 찾기 (8강, 16강 등)
        int targetRound = calculateTargetRound(totalImages); // 9~15면 8, 17~31이면 16 반환

        // 2. 예선전 경기 수 = (시작 개수 - 타겟 라운드)
        int matchCount = totalImages - targetRound;

        // 3. 예선전 참가자 수 = 경기 수 * 2
        int firstRoundPlayersCount = matchCount * 2;

        List<Long> shuffledIds = new ArrayList<>(imageIds);
        Collections.shuffle(shuffledIds);//리스트(List)의 요소를 무작위(Random)로 섞을 때(shuffle)

        List<MatchUpEntity> matchUps = new ArrayList<>();
        // 4. 실제 대결 (상태: NEED_VOTE)
        for (int i = 0; i < firstRoundPlayersCount; i += 2) {
            matchUps.add(MatchUpEntity.of(
                    new RealMatchUp(matchId, totalImages, shuffledIds.get(i), shuffledIds.get(i + 1))
            ));
        }
        // 5. 부전승 처리 (상태: SKIPPED)
        // 승자(winnerId)를 자기 자신으로 바로 등록하여 다음 라운드 자동 진출 처리
        for (int i = firstRoundPlayersCount; i < totalImages; i++) {
            matchUps.add(MatchUpEntity.of(
                    new AutoPassMatch(matchId, totalImages, shuffledIds.get(i))
            ));
        }
        return matchUps;
    }
    private int calculateTargetRound(int n) {
        int target = 1;
        // 다음 단계(target * 2)가 n을 넘지 않을 때까지만 계속 곱함
        while (target * 2 <= n) {
            target *= 2;
        }
        return target;
    }
}
