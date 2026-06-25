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
            // 입력장수, 기대하는 투표 매치(NEED_VOTE) 수, 기대하는 부전승(SKIPPED) 수
            "8,  4,  0",
            "9,  1,  7",
            "11, 3,  5",
            "15, 7,  1",
            "16, 8,  0",
            "17, 1,  15",
            "31, 15, 1",
            "32, 16, 0"
    })
    @DisplayName("상태별로 NEED_VOTE와 SKIPPED 매치 수가 정확히 생성되었는지 정밀 검증한다")
    void createMatches_Success_VerifyStates(int totalImages, int expectedMatches, int expectedByes) {
        // given
        Long matchId = 1L;
        List<Long> imageIds = LongStream.rangeClosed(1, totalImages)
                .boxed()
                .collect(Collectors.toList());

        // when
        List<MatchUpEntity> matchUps = matchUpCreator.createMatches(matchId, imageIds);

        // [확인용 로그]
        System.out.println("=== [입력 이미지: " + totalImages + "장] ===");
        System.out.println("투표해야 할 경기 수: " + expectedMatches + "회 / 자동 통과 수: " + expectedByes + "명");

        // then: NEED_VOTE와 SKIPPED 상태별 개수를 각각 정밀 검증
        long actualMatches = matchUps.stream()
                .filter(m -> m.getState() == MatchUpState.NEED_VOTE)
                .count();

        long actualByes = matchUps.stream()
                .filter(m -> m.getState() == MatchUpState.SKIPPED)
                .count();

        assertThat(actualMatches).as("NEED_VOTE 매치(경기) 수가 일치해야 함").isEqualTo(expectedMatches);
        assertThat(actualByes).as("SKIPPED(부전승) 수가 일치해야 함").isEqualTo(expectedByes);
        assertThat(matchUps).hasSize(expectedMatches + expectedByes);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 7, 33, 64})
    @DisplayName("8장 미만/32장 초과 시 ApiException(INVALID_IMAGE_COUNT)이 발생한다")
    void createMatches_Fail_OutOfBounds(int invalidSize) {
        // given
        Long matchId = 1L;
        List<Long> imageIds = LongStream.rangeClosed(1, invalidSize)
                .boxed()
                .collect(Collectors.toList());

        // when & then: ApiException 발생 여부 및 ErrorType 검증
        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
                matchUpCreator.createMatches(matchId, invalidSize < 8 ? null : imageIds)); // 예외 유도

        assertThat(thrown).isInstanceOf(ApiException.class);

        ApiException apiException = (ApiException) thrown;

        System.out.println("=== [예외 테스트: " + invalidSize + "장] ===");
        System.out.println("에러 타입: " + apiException.getErrorType());
        System.out.println("메시지: " + apiException.getMessage());

        assertThat(apiException.getErrorType()).isEqualTo(ErrorType.INVALID_IMAGE_COUNT);
    }
}