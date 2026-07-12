package com.team.moodit.domain.match;

import com.team.moodit.domain.PreferenceDetailTypeScore;
import com.team.moodit.domain.PreferenceTypeScore;
import com.team.moodit.storage.db.core.MatchPreferenceResultEntity;
import com.team.moodit.storage.db.core.MatchPreferenceResultRepository;
import com.team.moodit.storage.db.core.MatchResultEntity;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchResultFinder {
    private final MatchResultRepository matchResultRepository;
    private final MatchPreferenceResultRepository matchPreferenceResultRepository;

    public List<MatchResult> find(List<Long> matchIds) {
        List<MatchResultEntity> matchResults = matchResultRepository.findByMatchIdIn(matchIds);
        Map<Long, List<MatchPreferenceResultEntity>> matchPreferenceMap = matchPreferenceResultRepository.findByMatchResultIdIn(
                matchResults.stream().map(MatchResultEntity::getId).toList()
        ).stream().collect(Collectors.groupingBy(MatchPreferenceResultEntity::getMatchResultId));

        return matchResults.stream()
                .map(it ->
                        new MatchResult(
                                it.getId(),
                                it.getMatchId(),
                                it.getTitle(),
                                it.getRepresentativeMatchImageId(),
                                it.getRoundCount(),
                                it.getCompletedAt(),
                                new MatchPreferenceResult(
                                        it.getPreferenceResultType(),
                                        it.getPreferenceType(),
                                        it.getPreferenceDetailType(),
                                        matchPreferenceMap.get(it.getId()).stream().map(p ->
                                                new PreferenceTypeScore(
                                                        p.getPreferenceType(),
                                                        p.getPreferenceDetailType(),
                                                        p.getSelectedCount(),
                                                        p.getRank()
                                                )
                                        ).toList(),
                                        matchPreferenceMap.get(it.getId()).stream().map(p ->
                                                new PreferenceDetailTypeScore(
                                                        it.getPreferenceType(),
                                                        p.getPreferenceDetailType(),
                                                        p.getSelectedCount(),
                                                        p.getRank()
                                                )
                                        ).toList()
                                )
                        )
                ).toList();
    }

    public MatchResult find(Long userId, Long matchId) {
        MatchResultEntity matchResult = matchResultRepository.findByUserIdAndMatchId(userId, matchId)
                .orElseThrow(() -> new ApiException(ErrorType.MATCH_RESULT_NOT_FOUND));
        List<MatchPreferenceResultEntity> preferenceResults = matchPreferenceResultRepository.findByMatchResultId(matchResult.getId());

        return new MatchResult(
                matchResult.getId(),
                matchResult.getMatchId(),
                matchResult.getTitle(),
                matchResult.getRepresentativeMatchImageId(),
                matchResult.getRoundCount(),
                matchResult.getCompletedAt(),
                new MatchPreferenceResult(
                        matchResult.getPreferenceResultType(),
                        matchResult.getPreferenceType(),
                        matchResult.getPreferenceDetailType(),
                        preferenceResults.stream().map(it ->
                                new PreferenceTypeScore(
                                        it.getPreferenceType(),
                                        it.getPreferenceDetailType(),
                                        it.getSelectedCount(),
                                        it.getRank()
                                )
                        ).toList(),
                        preferenceResults.stream().map(it ->
                                new PreferenceDetailTypeScore(
                                        it.getPreferenceType(),
                                        it.getPreferenceDetailType(),
                                        it.getSelectedCount(),
                                        it.getRank()
                                )
                        ).toList()
                )
        );
    }

    public Long findOwnedMatchId(Long userId, Long matchResultId) {
        MatchResultEntity matchResult = matchResultRepository.findById(matchResultId)
                .orElseThrow(() -> new ApiException(ErrorType.MATCH_RESULT_NOT_FOUND));
        if (!matchResult.getUserId().equals(userId)) {
            throw new ApiException(ErrorType.MATCH_RESULT_NOT_FOUND);
        }

        return matchResult.getMatchId();
    }
}
