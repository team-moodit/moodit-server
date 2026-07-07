package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
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

    @Transactional
    public void deleteMatch(Long userId, Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        if (!match.getUserId().equals(userId)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        if (match.getState().equals(MatchState.DONE)) {
            throw new ApiException(ErrorType.MATCH_INVALID_STATE);
        }

        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);
        matchChoiceRepository.deleteByMatchUpIdIn(matchUps.stream().map(MatchUpEntity::getId).toList());

        matchVoteCandidateRepository.deleteByMatchId(matchId);
        matchUpRepository.deleteByMatchId(matchId);
        matchRepository.delete(match);
    }
}
