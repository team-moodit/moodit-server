package com.team.moodit.domain.matchup;

import com.team.moodit.domain.match.MatchUpCreator;
import com.team.moodit.domain.match.MatchUpCreateResult;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import com.team.moodit.storage.db.core.MatchVoteEntity;
import lombok.extern.slf4j.Slf4j;
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
    @ValueSource(ints = {8, 16, 32}) // 기획 사양에 맞춰 8강, 16강, 32강 기준으로 정밀 테스트
    @DisplayName("이미지 개수별 대진 생성 및 피그마 반영 질문 4개 순환/랜덤 시스템 로그 확인 테스트")
    void createMatches_PrintLogs(int imageCount) {
        // given
        List<Long> imageIds = createDummyImageIds(imageCount);
        List<MatchVoteEntity> mockTemplates = createMockVoteTemplates(); // 리플렉션 기반 가짜 데이터 풀 주입

        log.info("==================================================");
        log.info("[테스트 시작] 입력 이미지 개수: {}장 ({}강전)", imageCount, imageCount);
        log.info("==================================================");

        // when
        // 도메인 계층의 조립 메서드 호출
        MatchUpCreateResult result = matchUpCreator.createMatches(TEST_MATCH_ID, imageIds, mockTemplates);

        List<MatchUpEntity> matchUps = result.getMatchUps();
        List<MatchVoteCandidateEntity> voteCandidates = result.getVoteCandidates();

        // 내부 공식 역산해서 로그용 변수 추출
        int targetRound = Integer.highestOneBit(imageCount - 1);
        int matchCount = imageCount - targetRound;
        int firstRoundPlayersCount = matchCount * 2;
        int autoPassCount = imageCount - firstRoundPlayersCount;
        int totalMatchRounds = imageCount - 1;

        // then
        log.info(" [대진표 연산 결과]");
        log.info(" - 실제 치를 경기 수            : {}경기", matchCount);
        log.info(" - 부전승 플레이어 수           : {}명", autoPassCount);
        log.info(" - 총 생성된 MatchUpEntity 개수 : {}개", matchUps.size());
        log.info("--------------------------------------------------");
        log.info(" [ 피그마 UI 기준 질문 조립 결과 - 총 {}판]", totalMatchRounds);

        // 4개씩 묶어서 라운드별(판별)로 로그 출력
        for (int i = 0; i < voteCandidates.size(); i += 4) {
            int roundNum = voteCandidates.get(i).getRoundNumber();
            log.info("  ▶ [ {} 라운드 (판)]", roundNum);
            log.info("    1번 칸 (적합도): [{}] {}", voteCandidates.get(i).getPreferenceDetail(), voteCandidates.get(i).getContent());
            log.info("    2번 칸 (심미성): [{}] {}", voteCandidates.get(i + 1).getPreferenceDetail(), voteCandidates.get(i + 1).getContent());
            log.info("    3번 칸 (지속성): [{}] {}", voteCandidates.get(i + 2).getPreference(), voteCandidates.get(i + 2).getContent());
            log.info("    4번 칸 (트렌드): [{}] {}", voteCandidates.get(i + 3).getPreference(), voteCandidates.get(i + 3).getContent());
        }
        log.info("==================================================");

        // 핵심 검증문
        assertThat(matchUps).hasSize(matchCount + autoPassCount);
        assertThat(voteCandidates).hasSize(totalMatchRounds * 4); // 판수 x 4개 형태 유효성 체크
    }

    private List<Long> createDummyImageIds(int size) {
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= size; i++) {
            ids.add(i);
        }
        return ids;
    }

    /**
     * 리플렉션을 이용해 protected 엔티티의 필드를 강제로 세팅하여 가짜 데이터 풀을 빌드합니다.
     */
    private List<MatchVoteEntity> createMockVoteTemplates() {
        List<MatchVoteEntity> templates = new ArrayList<>();

        templates.add(createTestEntity(70L, "나한테 잘 어울릴 거 같아서", "FITNESS", "BODY_FIT"));
        templates.add(createTestEntity(72L, "내 신체적 특징을 잘 보완해줘서", "FITNESS", "BODY_FIT"));
        templates.add(createTestEntity(98L, "내 추구미와 잘 맞아서", "FITNESS", "VIBE"));
        templates.add(createTestEntity(99L, "내 평소 분위기와 더 비슷해서", "FITNESS", "VIBE"));
        templates.add(createTestEntity(112L, "매치하기 쉬울 것 같아서", "FITNESS", "MATCHABLE"));
        templates.add(createTestEntity(114L, "다른 아이템들과 코디하기 편할 것 같아서", "FITNESS", "MATCHABLE"));
        templates.add(createTestEntity(59L, "색감이 더 마음에 들어서", "AESTHETICS", "COLOR"));
        templates.add(createTestEntity(38L, "디자인이 더 마음에 들어서", "AESTHETICS", "DESIGN"));
        templates.add(createTestEntity(48L, "전체적인 분위기가 더 좋아서", "AESTHETICS", "MOOD"));
        templates.add(createTestEntity(82L, "꾸준히 손이 갈 것 같아서", "CONSISTENCE", null));
        templates.add(createTestEntity(120L, "요즘 자주 보이는 스타일이라서", "TREND", null));

        return templates;
    }

    /**
     *  [치트키 헬퍼 메서드] 리플렉션으로 private 필드에 값을 강제 주입합니다.
     */
    private MatchVoteEntity createTestEntity(Long id, String content, String preference, String preferenceDetail) {
        try {
            // 1. protected 기본 생성자를 호출하여 인스턴스 생성
            java.lang.reflect.Constructor<MatchVoteEntity> constructor = MatchVoteEntity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            MatchVoteEntity entity = constructor.newInstance();

            // 2. 리플렉션으로 private 필드들 강제 세팅
            java.lang.reflect.Field contentField = MatchVoteEntity.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(entity, content);

            java.lang.reflect.Field preferenceField = MatchVoteEntity.class.getDeclaredField("preference");
            preferenceField.setAccessible(true);
            preferenceField.set(entity, preference);

            java.lang.reflect.Field preferenceDetailField = MatchVoteEntity.class.getDeclaredField("preferenceDetail");
            preferenceDetailField.setAccessible(true);
            preferenceDetailField.set(entity, preferenceDetail);

            // 3. 부모 클래스(BaseIdEntity)에 선언되어 있을 id 필드 세팅
            java.lang.reflect.Field idField = com.team.moodit.storage.db.core.BaseIdEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);

            return entity;
        } catch (Exception e) {
            throw new RuntimeException("테스트 엔티티 생성 중 리플렉션 에러 발생", e);
        }
    }
}