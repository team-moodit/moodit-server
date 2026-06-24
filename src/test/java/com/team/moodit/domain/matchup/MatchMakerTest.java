package com.team.moodit.domain.matchup;

import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.domain.match.MatchMaker;
import com.team.moodit.storage.db.core.MatchUpEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class MatchMakerTest {
    private final MatchMaker matchMaker = new MatchMaker();

    @Test
    @DisplayName("11장의 사진이 들어오면 3경기와 5개의 부전승 매치가 생성되어야 한다")
    void shouldGenerateCorrectMatchUpsFor11Images() {
        // given
        Long matchId = 1L;
        List<Long> imageIds = LongStream.rangeClosed(1, 15).boxed().toList();

        // when
        List<MatchUpEntity> results = matchMaker.createMatches(matchId, imageIds);

        // then
        // 11개 -> 8강 목표 -> 3경기(NEED_VOTE) + 5부전승(SKIPPED) = 총 8개
        System.out.println("=== 15개 입력시 생성된 결과 ===");
        results.forEach(m -> {
            System.out.println("상태: " + m.getState() +
                    " | 후보A: " + m.getCandidateAId() +
                    " | 후보B: " + m.getCandidateBId() +
                    " | 승자: " + m.getWinnerId());
        });
        System.out.println("========================");
        assertThat(results).hasSize(8);

        long voteMatches = results.stream()
                .filter(m -> m.getState() == MatchUpState.NEED_VOTE).count();
        long skippedMatches = results.stream()
                .filter(m -> m.getState() == MatchUpState.SKIPPED).count();

        assertThat(voteMatches).isEqualTo(7);
        assertThat(skippedMatches).isEqualTo(1);
    }

    @Test
    @DisplayName("31개의 사진이 들어올 때 매치업 생성 과정 출력")
    void printMatchUpProcessFor31Images() {
        // given
        Long matchId = 1L;
        List<Long> imageIds = LongStream.rangeClosed(1, 31).boxed().toList();

        // when
        List<MatchUpEntity> results = matchMaker.createMatches(matchId, imageIds);

        // then
        System.out.println("=== 31개 입력 시 생성 결과 ===");
        results.forEach(m -> {
            String candidateB = (m.getCandidateBId() != null) ? String.valueOf(m.getCandidateBId()) : "null";
            System.out.println("상태: " + m.getState() +
                    " | 후보A: " + m.getCandidateAId() +
                    " | 후보B: " + candidateB +
                    " | 승자: " + (m.getWinnerId() != null ? m.getWinnerId() : "null"));
        });
        System.out.println("========================");
    }

}
