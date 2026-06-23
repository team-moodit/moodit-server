package com.team.moodit.domain.match;

import com.team.moodit.domain.PreferenceTypeScore;
import com.team.moodit.storage.db.core.MatchPreferenceResultEntity;
import com.team.moodit.storage.db.core.MatchPreferenceResultRepository;
import com.team.moodit.storage.db.core.MatchResultEntity;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchResultReader {
    private final MatchResultRepository matchResultRepository;
    private final MatchPreferenceResultRepository matchPreferenceResultRepository;

    public MatchResult getMatchResult(Long userId, Long matchId) {
        MatchResultEntity matchResult = matchResultRepository.findByUserIdAndMatchId(userId, matchId).orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND_MATCH_RESULT));
        List<MatchPreferenceResultEntity> preferenceResults = matchPreferenceResultRepository.findByMatchResultId(matchResult.getId());

        return new MatchResult(
                matchResult.getMatchId(),
                matchResult.getTitle(),
                matchResult.getRepresentativeImageId(),
                matchResult.getRoundCount(),
                matchResult.getCompletedAt(),
                new MatchPreferenceResult(
                        matchResult.getPreferenceResultType(),
                        matchResult.getPreferenceType(),
                        matchResult.getPreferenceDetailType(),
                        preferenceResults.stream().map(it ->
                              new PreferenceTypeScore(
                                      it.getPreferenceType(),
                                      it.getSelectedCount(),
                                      it.getRank()
                              )
                        ).toList()
                )
        );
    }
}
