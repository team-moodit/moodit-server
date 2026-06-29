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

    private static final double EPSILON = 0.05;

    public MatchPreferenceAnalysis analyze(List<MatchVoteCandidateEntity> votedCandidates) {
        int totalSelectedCount = votedCandidates.size();

        // 1. 대표 상위 선호(Preference) 집계
        Map<String, Long> mainCountMap = votedCandidates.stream()
                .collect(Collectors.groupingBy(MatchVoteCandidateEntity::getPreference, Collectors.counting()));

        List<LabelStatistics> mainStats = mainCountMap.entrySet().stream()
                .map(e -> new LabelStatistics(e.getKey(), e.getValue().intValue(), (double) e.getValue() / totalSelectedCount))
                .sorted(Comparator.comparingDouble(LabelStatistics::getRatio).reversed())
                .toList();

        List<PreferenceRankDto> ranks = new ArrayList<>();
        for (int i = 0; i < mainStats.size(); i++) {
            LabelStatistics stats = mainStats.get(i);
            ranks.add(new PreferenceRankDto(stats.getLabel(), i + 1, stats.getSelectedCount()));
        }

        boolean isMainClear = mainStats.size() == 1 || (mainStats.get(0).getRatio() - mainStats.get(1).getRatio() > EPSILON);

        // 분기 1. 상위 1위가 확실한 경우
        if (isMainClear) {
            String targetMain = mainStats.get(0).getLabel();
            List<MatchVoteCandidateEntity> subFiltered = votedCandidates.stream()
                    .filter(c -> targetMain.equals(c.getPreference()) && c.getPreferenceDetail() != null && !c.getPreferenceDetail().isBlank())
                    .toList();

            if (!subFiltered.isEmpty()) {
                int subTotal = subFiltered.size();
                Map<String, Long> subCountMap = subFiltered.stream()
                        .collect(Collectors.groupingBy(MatchVoteCandidateEntity::getPreferenceDetail, Collectors.counting()));

                List<LabelStatistics> subStats = subCountMap.entrySet().stream()
                        .map(e -> new LabelStatistics(e.getKey(), e.getValue().intValue(), (double) e.getValue() / subTotal))
                        .sorted(Comparator.comparingDouble(LabelStatistics::getRatio).reversed())
                        .toList();

                boolean isSubClear = subStats.size() == 1 || (subStats.get(0).getRatio() - subStats.get(1).getRatio() > EPSILON);

                if (isSubClear) {
                    return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_AND_DETAIL, targetMain, subStats.get(0).getLabel(), ranks);
                }
            }
            return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_ONLY, targetMain, null, ranks);
        }

        // 분기 2. 상위가 박빙인 경우, 전체 데이터에서 디테일 우선 탐색
        List<MatchVoteCandidateEntity> allSubs = votedCandidates.stream()
                .filter(c -> c.getPreferenceDetail() != null && !c.getPreferenceDetail().isBlank())
                .toList();

        if (!allSubs.isEmpty()) {
            int allSubTotal = allSubs.size();
            Map<String, Long> allSubCountMap = allSubs.stream()
                    .collect(Collectors.groupingBy(MatchVoteCandidateEntity::getPreferenceDetail, Collectors.counting()));

            List<LabelStatistics> allSubStats = allSubCountMap.entrySet().stream()
                    .map(e -> new LabelStatistics(e.getKey(), e.getValue().intValue(), (double) e.getValue() / allSubTotal))
                    .sorted(Comparator.comparingDouble(LabelStatistics::getRatio).reversed())
                    .toList();

            boolean isGlobalSubClear = allSubStats.size() == 1 || (allSubStats.get(0).getRatio() - allSubStats.get(1).getRatio() > EPSILON);

            if (isGlobalSubClear) {
                String bestDetail = allSubStats.get(0).getLabel();
                String parentalMain = votedCandidates.stream()
                        .filter(c -> bestDetail.equals(c.getPreferenceDetail()))
                        .map(MatchVoteCandidateEntity::getPreference)
                        .findFirst()
                        .orElse(mainStats.get(0).getLabel());

                return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_ONLY, parentalMain, bestDetail, ranks);
            }
        }

        return new MatchPreferenceAnalysis(PreferenceResultType.TIE, null, null, ranks);
    }

    private class LabelStatistics {
        private final String label;
        private final int selectedCount;
        private final double ratio;

        public LabelStatistics(String label, int selectedCount, double ratio) {
            this.label = label;
            this.selectedCount = selectedCount;
            this.ratio = ratio;
        }

        public String getLabel() { return label; }
        public int getSelectedCount() { return selectedCount; }
        public double getRatio() { return ratio; }
    }
}