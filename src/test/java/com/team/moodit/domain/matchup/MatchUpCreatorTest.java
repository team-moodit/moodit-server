package com.team.moodit.domain.matchup;

import com.team.moodit.domain.match.MatchUpCreator;
import com.team.moodit.storage.db.core.MatchUpEntity;
import lombok.extern.slf4j.Slf4j; // 📌 Lombok의 Slf4j 어노테이션 추가
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class MatchUpCreatorTest {

    private final MatchUpCreator matchUpCreator = new MatchUpCreator();
    private final Long TEST_MATCH_ID = 1L;

    @ParameterizedTest
    @ValueSource(ints = {15, 31, 16})
    @DisplayName("이미지 개수별 대진 생성 및 부전승 처리 시스템 로그 확인 테스트")
    void createMatches_PrintLogs(int imageCount) {
        // given
        List<Long> imageIds = createDummyImageIds(imageCount);

        log.info("==================================================");
        log.info("[테스트 시작] 입력 이미지 개수: {}장", imageCount);
        log.info("==================================================");

        // when
        List<MatchUpEntity> matchUps = matchUpCreator.createMatches(TEST_MATCH_ID, imageIds);

        // 내부 공식 역산해서 로그용 변수 추출
        int targetRound = Integer.highestOneBit(imageCount - 1);
        int matchCount = imageCount - targetRound;
        int firstRoundPlayersCount = matchCount * 2;
        int autoPassCount = imageCount - firstRoundPlayersCount;

        // then
        log.info(" [중간 연산 결과]");
        log.info(" - targetRound (기준 라운드 비트) : {}", targetRound);
        log.info(" - matchCount  (실제 치를 경기 수) : {}", matchCount);
        log.info(" - 예선 참가 플레이어 수          : {}명", firstRoundPlayersCount);
        log.info(" - 부전승(Auto Pass) 플레이어 수  : {}명", autoPassCount);
        log.info("--------------------------------------------------");
        log.info(" [최종 매치업 엔티티 생성 결과]");
        log.info(" - 총 생성된 MatchUpEntity 개수 : {}개", matchUps.size());
        log.info("==================================================");

        // 검증 코드
        assertThat(matchUps).hasSize(matchCount + autoPassCount);
    }

    private List<Long> createDummyImageIds(int size) {
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= size; i++) {
            ids.add(i);
        }
        return ids;
    }
}