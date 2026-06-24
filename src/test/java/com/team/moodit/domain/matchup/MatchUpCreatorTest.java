package com.team.moodit.domain.matchup;

import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.domain.match.MatchUpCreator;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchUpCreatorTest {

    private final MatchUpCreator matchUpCreator = new MatchUpCreator();

    @ParameterizedTest
    @CsvSource({
            // 입력장수, 기대하는 투표 매치(경기) 수, 기대하는 부전승 수
            "8,  4,  0",
            "9,  1,  7",
            "11, 3,  5",
            "15, 7,  1",
            "16, 8,  0",
            "17, 1,  15",
            "31, 15, 1",
            "32, 16, 0"
    })
    @DisplayName("상태별로 투표 매치(NEED_VOTE)와 부전승(SKIPPED) 매치 수가 정확히 생성되었는지 정밀 검증한다")
    void createMatches_Success_VerifyStates(int totalImages, int expectedMatches, int expectedByes) {
        // given
        Long matchId = 1L;
        List<Long> imageIds = LongStream.rangeClosed(1, totalImages)
                .boxed()
                .collect(Collectors.toList());

        // when
        List<MatchUpEntity> matchUps = matchUpCreator.createMatches(matchId, imageIds);

        // 디버깅용 확인 로그
        System.out.println("=== [입력 이미지: " + totalImages + "장] ===");
        System.out.println("예선해야 할 경기 수: " + expectedMatches + "회");
        System.out.println("자동 통과(부전승) 수: " + expectedByes + "명");

        // then: [리뷰어 피드백 반영] 상태별 개수 정밀 검증
        long actualMatches = matchUps.stream()
                .filter(m -> m.getState() == MatchUpState.NEED_VOTE)
                .count();

        long actualByes = matchUps.stream()
                .filter(m -> m.getState() == MatchUpState.SKIPPED)
                .count();

        // 검증 로직
        assertThat(actualMatches)
                .as("투표 매치(경기) 수가 일치해야 함")
                .isEqualTo(expectedMatches);

        assertThat(actualByes)
                .as("부전승 수와 일치해야 함")
                .isEqualTo(expectedByes);

        assertThat(matchUps).hasSize(expectedMatches + expectedByes);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 7, 33, 64})
    @DisplayName("8장 미만/32장 초과 시 ApiException이 발생한다")
    void createMatches_Fail_OutOfBounds(int invalidSize) {
        // given
        Long matchId = 1L;
        List<Long> imageIds = LongStream.rangeClosed(1, invalidSize)
                .boxed()
                .collect(Collectors.toList());

        // 1. catchThrowable을 사용하여 예외를 변수에 담습니다 (타입 캐스팅 문제 해결!)
        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
                matchUpCreator.createMatches(matchId, imageIds));

        // 2. 예외가 ApiException인지 검증
        assertThat(thrown).isInstanceOf(ApiException.class);

        // 3. 이제 안전하게 캐스팅해서 에러 타입과 메시지 확인 및 출력
        ApiException apiException = (ApiException) thrown;

        System.out.println("=== [입력 이미지: " + invalidSize + "장] ===");
        System.out.println("발생한 에러 타입: " + apiException.getErrorType());
        System.out.println("에러 메시지: " + apiException.getMessage());

        // 4. 추가 검증: 에러 타입이 INVALID_IMAGE_COUNT인지 확인
        assertThat(apiException.getErrorType()).isEqualTo(ErrorType.INVALID_IMAGE_COUNT);
    }
}