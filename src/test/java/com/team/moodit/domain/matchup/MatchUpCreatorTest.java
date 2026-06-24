package com.team.moodit.domain.matchup;

import com.team.moodit.domain.match.MatchUpCreator;
import com.team.moodit.storage.db.core.MatchUpEntity;
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
            // 입력장수, 예선전 매치 수, 부전승 엔티티 수
            "8,  0,  8",   // 8장: 예선 없이 8명 전원 부전승 엔티티 생성 -> 총 8개
            "9,  1,  7",   // 9장: 예선 1경기(2명) + 부전승 7명 -> 총 8개
            "11, 3,  5",   // 11장: 예선 3경기(6명) + 부전승 5명 -> 총 8개
            "15, 7,  1",   // 15장: 예선 7경기(14명) + 부전승 1명 -> 총 8개
            "16, 0,  16",  // 16장: 예선 없이 16명 전원 부전승 엔티티 생성 -> 총 16개
            "17, 1,  15",  // 17장: 예선 1경기(2명) + 부전승 15명 -> 총 16개
            "31, 15, 1",   // 31장: 예선 15경기(30명) + 부전승 1명 -> 총 16개
            "32, 0,  32"   // 32장: 예선 없이 32명 전원 부전승 엔티티 생성 -> 총 32개
    })
    @DisplayName("8장부터 32장 사이의 입력에 대해 올바른 대진 및 부전승 매치업 수가 생성된다")
    void createMatches_Success_WithinBounds(int totalImages, int expectedRealMatches, int expectedByes) {
        // given
        Long matchId = 1L;
        List<Long> imageIds = LongStream.rangeClosed(1, totalImages)
                .boxed()
                .collect(Collectors.toList());

        // when
        List<MatchUpEntity> matchUps = matchUpCreator.createMatches(matchId, imageIds);

        // then
        // 생성된 전체 매치업 엔티티 총 개수 검증 (예선전 수 + 부전승 수)
        int expectedTotalMatches = expectedRealMatches + expectedByes;
        assertThat(matchUps).hasSize(expectedTotalMatches);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 7, 33, 64})
    @DisplayName("8장 미만이거나 32장을 초과하면 예외가 발생한다")
    void createMatches_Fail_OutOfBounds(int invalidSize) {
        // given
        Long matchId = 1L;
        List<Long> imageIds = LongStream.rangeClosed(1, invalidSize)
                .boxed()
                .collect(Collectors.toList());

        // when & then
        assertThatThrownBy(() -> matchUpCreator.createMatches(matchId, imageIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("토너먼트는 최소 8장, 최대 32장의 이미지만 참여 가능합니다.");
    }
}