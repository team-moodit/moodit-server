package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MatchResultAnalyzer {

    public MatchPreferenceAnalysis analyze(List<MatchVoteCandidateEntity> votedCandidates) {
        if (votedCandidates == null || votedCandidates.isEmpty()) {
            return new MatchPreferenceAnalysis(
                    PreferenceResultType.TIE,
                    null,
                    null,
                    List.of()
            );
        }

        Map<String, Long> mainCountMap = votedCandidates.stream()
                .collect(Collectors.groupingBy(
                        MatchVoteCandidateEntity::getPreference,
                        Collectors.counting()
                ));

        List<LabelStatistics> mainStats = mainCountMap.entrySet().stream()
                .map(e -> new LabelStatistics(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator.comparingInt(LabelStatistics::count).reversed())
                .toList();

        List<PreferenceRankDto> ranks = new ArrayList<>();
        for (int i = 0; i < mainStats.size(); i++) {
            ranks.add(new PreferenceRankDto(
                    mainStats.get(i).label(),
                    i + 1,
                    mainStats.get(i).count()
            ));
        }

        boolean isMainTie =
                mainStats.size() > 1
                        && mainStats.get(0).count() == mainStats.get(1).count();

        if (isMainTie) {
            return new MatchPreferenceAnalysis(
                    PreferenceResultType.TIE,
                    null,
                    null,
                    ranks
            );
        }

        String targetMain = mainStats.get(0).label();

        if ("CONSISTENCE".equals(targetMain) || "TREND".equals(targetMain)) {
            return new MatchPreferenceAnalysis(
                    PreferenceResultType.TYPE_ONLY,
                    targetMain,
                    null,
                    ranks
            );
        }

        List<MatchVoteCandidateEntity> subFiltered = votedCandidates.stream()
                .filter(c -> targetMain.equals(c.getPreference()))
                .filter(c -> c.getPreferenceDetail() != null && !c.getPreferenceDetail().isBlank())
                .toList();

        if (!subFiltered.isEmpty()) {
            Map<String, Long> subCountMap = subFiltered.stream()
                    .collect(Collectors.groupingBy(
                            MatchVoteCandidateEntity::getPreferenceDetail,
                            Collectors.counting()
                    ));

            List<LabelStatistics> subStats = subCountMap.entrySet().stream()
                    .map(e -> new LabelStatistics(e.getKey(), e.getValue().intValue()))
                    .sorted(Comparator.comparingInt(LabelStatistics::count).reversed())
                    .toList();

            boolean isSubTie =
                    subStats.size() > 1
                            && subStats.get(0).count() == subStats.get(1).count();

            if (!isSubTie) {
                return new MatchPreferenceAnalysis(
                        PreferenceResultType.TYPE_AND_DETAIL,
                        targetMain,
                        subStats.get(0).label(),
                        ranks
                );
            }
        }

        return new MatchPreferenceAnalysis(
                PreferenceResultType.TYPE_ONLY,
                targetMain,
                null,
                ranks
        );
    }

    private record LabelStatistics(String label, int count) {
    }
}