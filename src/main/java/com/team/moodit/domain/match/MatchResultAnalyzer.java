package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;

public class MatchResultAnalyzer {

    private static final double EPSILON = 0.05; // 5% 오차범위 격차 기준

    public static MatchPreferenceAnalysis analyze(List<MatchVoteCandidateEntity> votedCandidates) {
        int totalSelectedCount = votedCandidates.size();

        // 1. 대표 상위 4개 선호(Preference) 선택 횟수 집계 및 정렬
        Map<String, Long> mainCountMap = votedCandidates.stream()
                .collect(Collectors.groupingBy(MatchVoteCandidateEntity::getPreference, Collectors.counting()));

        List<LabelStatistics> mainStats = mainCountMap.entrySet().stream()
                .map(e -> new LabelStatistics(e.getKey(), e.getValue().intValue(), (double) e.getValue() / totalSelectedCount))
                .sorted(Comparator.comparingDouble(LabelStatistics::getRatio).reversed())
                .toList();

        // 영속성 레이어 저장용 상위 랭킹 DTO 리스트 생성
        List<PreferenceRankDto> ranks = new ArrayList<>();
        for (int i = 0; i < mainStats.size(); i++) {
            LabelStatistics stats = mainStats.get(i);
            ranks.add(new PreferenceRankDto(stats.getLabel(), i + 1, stats.getSelectedCount()));
        }

        // 상위 1위와 2위의 비율 격차 검사 (5% 초과 격차면 상위 1위 확실)
        boolean isMainClear = mainStats.size() == 1 || (mainStats.get(0).getRatio() - mainStats.get(1).getRatio() > EPSILON);

        // =======================================================================
        // 분기 1. [선호 O] 상위 4개 중 확실한 1등이 존재하는 경우
        // =======================================================================
        if (isMainClear) {
            String targetMain = mainStats.get(0).getLabel();

            // 확정된 상위 라벨 내부의 하위 상세선호(Detail) 집계 및 정렬
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

                // 하위 상세선호도 1위와 2위의 격차가 5%를 초과하며 확실한가?
                boolean isSubClear = subStats.size() == 1 || (subStats.get(0).getRatio() - subStats.get(1).getRatio() > EPSILON);

                if (isSubClear) {
                    //  Case 1: 상위도 확실하고 하위도 확실함 (선호O 상세선호O)
                    return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_AND_DETAIL, targetMain, subStats.get(0).getLabel(), ranks);
                }
            }

            // 상위는 확실한데 하위 디테일이 박빙이거나 없는 경우 (기본 방어벽)
            return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_ONLY, targetMain, null, ranks);
        }

        // =======================================================================
        // 분기 2. [선호 X] 대표 4개 선호가 동률/박빙인 경우
        // =======================================================================
        // 전체 투표 데이터에서 '하위 상세선호(Detail)'들만 전부 긁어모아 전체 카운트 계산
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

            // 상위를 떼고 하위 상세선호 항목들끼리만 정렬했을 때 독보적인 1위가 있는가?
            boolean isGlobalSubClear = allSubStats.size() == 1 || (allSubStats.get(0).getRatio() - allSubStats.get(1).getRatio() > EPSILON);

            if (isGlobalSubClear) {
                //  Case 2: 상위는 동률이지만 하위 상세선호 중 확실한 1등이 존재 (선호X 상세선호O)
                String bestDetail = allSubStats.get(0).getLabel();

                // 해당 하위 상세선호가 원래 속해있던 상위 카테고리를 역추적하여 매핑
                String parentalMain = votedCandidates.stream()
                        .filter(c -> bestDetail.equals(c.getPreferenceDetail()))
                        .map(MatchVoteCandidateEntity::getPreference)
                        .findFirst()
                        .orElse(mainStats.get(0).getLabel());

                // 기획 분기상 선호X 상세선호O 이므로 타입은 TYPE_ONLY 등으로 프론트와 맞춰 분기
                return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_ONLY, parentalMain, bestDetail, ranks);
            }
        }

        //  Case 3: 상위도 박빙이고 하위 상세선호마저도 뚜렷한 기준이 없음 (선호X 상세선호X)
        return new MatchPreferenceAnalysis(PreferenceResultType.TIE, null, null, ranks);
    }

    private static class LabelStatistics {
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