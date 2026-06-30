package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.*;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchRemover {

    private final MatchRepository matchRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchVoteCandidateRepository matchVoteCandidateRepository;
    private final MatchChoiceRepository matchChoiceRepository;
    private final MatchResultRepository matchResultRepository;
    private final MatchPreferenceResultRepository preferenceResultRepository;

    @Transactional
    public void deleteMatch(Long userId, Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 도메인 권한 검증
        if (!match.getUserId().equals(userId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        // 연쇄 삭제 흐름 수행
        matchResultRepository.findByUserIdAndMatchId(userId, matchId).ifPresent(result -> {
            preferenceResultRepository.deleteByMatchResultId(result.getId());
        });
        matchResultRepository.deleteByMatchId(matchId);

        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);
        if (matchUps != null && !matchUps.isEmpty()) {
            for (MatchUpEntity mu : matchUps) {
                matchChoiceRepository.deleteByMatchUpId(mu.getId());
            }
        }

        matchVoteCandidateRepository.deleteByMatchId(matchId);
        matchUpRepository.deleteByMatchId(matchId);

        matchRepository.delete(match);
    }
}