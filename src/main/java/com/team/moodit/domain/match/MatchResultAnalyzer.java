package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

        List<PreferenceRankDto> ranks = createRanks(mainStats);

        boolean isMainTie =
                mainStats.size() > 1
                        && mainStats.get(0).count() == mainStats.get(1).count();

        if (isMainTie) {
            List<String> topMainPreferences = mainStats.stream()
                    .filter(stat -> stat.count() == mainStats.get(0).count())
                    .map(LabelStatistics::label)
                    .toList();

            List<String> detailSupportedPreferences = topMainPreferences.stream()
                    .filter(this::hasDetailPreference)
                    .toList();

            if (!detailSupportedPreferences.isEmpty()) {
                MatchPreferenceAnalysis detailTieResult = analyzeDetailTie(
                        votedCandidates,
                        detailSupportedPreferences,
                        ranks
                );

                if (detailTieResult != null) {
                    return detailTieResult;
                }
            }

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

    private List<PreferenceRankDto> createRanks(List<LabelStatistics> mainStats) {
        List<PreferenceRankDto> ranks = new ArrayList<>();

        int rank = 1;

        for (int i = 0; i < mainStats.size(); i++) {
            if (i > 0 && mainStats.get(i).count() != mainStats.get(i - 1).count()) {
                rank++;
            }
            ranks.add(new PreferenceRankDto(
                    mainStats.get(i).label(),
                    rank,
                    mainStats.get(i).count()
            ));
        }
        return ranks;
    }


    private record LabelStatistics(String label, int count) {
    }


    private boolean hasDetailPreference(String preference) {
        return PreferenceType.from(preference).hasDetail();
    }

    private MatchPreferenceAnalysis analyzeDetailTie(
            List<MatchVoteCandidateEntity> votedCandidates,
            List<String> detailSupportedPreferences,
            List<PreferenceRankDto> ranks
    ) {
        List<MatchVoteCandidateEntity> detailCandidates = votedCandidates.stream()
                .filter(candidate -> detailSupportedPreferences.contains(candidate.getPreference()))
                .filter(candidate -> candidate.getPreferenceDetail() != null)
                .filter(candidate -> !candidate.getPreferenceDetail().isBlank())
                .toList();

        if (detailCandidates.isEmpty()) {
            return null;
        }

        Map<String, Long> detailCountMap = detailCandidates.stream()
                .collect(Collectors.groupingBy(
                        MatchVoteCandidateEntity::getPreferenceDetail,
                        Collectors.counting()
                ));

        long maxDetailCount = detailCountMap.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        List<String> topDetails = detailCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxDetailCount)
                .map(Map.Entry::getKey)
                .toList();

        String selectedDetail = topDetails.get(
                new Random().nextInt(topDetails.size())
        );

        String selectedPreference = detailCandidates.stream()
                .filter(candidate -> selectedDetail.equals(candidate.getPreferenceDetail()))
                .map(MatchVoteCandidateEntity::getPreference)
                .findAny()
                .orElse(null);

        if (selectedPreference == null) {
            return null;
        }

        return new MatchPreferenceAnalysis(
                PreferenceResultType.TYPE_AND_DETAIL,
                selectedPreference,
                selectedDetail,
                ranks
        );
    }
}