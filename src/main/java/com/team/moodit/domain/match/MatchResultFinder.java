package com.team.moodit.domain.match;

import com.team.moodit.domain.PreferenceTypeScore;
import com.team.moodit.storage.db.core.MatchPreferenceResultEntity;
import com.team.moodit.storage.db.core.MatchPreferenceResultRepository;
import com.team.moodit.storage.db.core.MatchResultEntity;
import com.team.moodit.storage.db.core.MatchResultRepository;
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
                                                        p.getSelectedCount(),
                                                        p.getRank()
                                                )
                                        ).toList()
                                )
                        )
                ).toList();
    }
}
