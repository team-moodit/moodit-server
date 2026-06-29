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
            return new MatchPreferenceAnalysis(PreferenceResultType.TIE, null, null, List.of());
        }

        // 1. 상위 선호 집계 및 득표수 내림차순 정렬
        Map<String, Long> mainCountMap = votedCandidates.stream()
                .collect(Collectors.groupingBy(MatchVoteCandidateEntity::getPreference, Collectors.counting()));

        List<LabelStatistics> mainStats = mainCountMap.entrySet().stream()
                .map(e -> new LabelStatistics(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator.comparingInt(LabelStatistics::count).reversed())
                .toList();

        List<PreferenceRankDto> ranks = new ArrayList<>();
        for (int i = 0; i < mainStats.size(); i++) {
            ranks.add(new PreferenceRankDto(mainStats.get(i).label(), i + 1, mainStats.get(i).count()));
        }

        // 최다 득표가 동점일 경우 뚜렷한 기준 없음 처리
        boolean isMainTie = mainStats.size() > 1 && (mainStats.get(0).count() == mainStats.get(1).count());
        if (isMainTie) {
            return new MatchPreferenceAnalysis(PreferenceResultType.TIE, null, null, ranks);
        }

        String targetMain = mainStats.get(0).label();

        // 지속성, 트렌드는 하위 라벨이 없으므로 자기 자신을 하위 라벨로 취급
        if ("지속성".equals(targetMain) || "트렌드".equals(targetMain)) {
            return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_ONLY, targetMain, targetMain, ranks);
        }

        // 2. 심미성, 나와의 적합도 상세 선호 분석
        List<MatchVoteCandidateEntity> subFiltered = votedCandidates.stream()
                .filter(c -> targetMain.equals(c.getPreference()) && c.getPreferenceDetail() != null && !c.getPreferenceDetail().isBlank())
                .toList();

        if (!subFiltered.isEmpty()) {
            Map<String, Long> subCountMap = subFiltered.stream()
                    .collect(Collectors.groupingBy(MatchVoteCandidateEntity::getPreferenceDetail, Collectors.counting()));

            List<LabelStatistics> subStats = subCountMap.entrySet().stream()
                    .map(e -> new LabelStatistics(e.getKey(), e.getValue().intValue()))
                    .sorted(Comparator.comparingInt(LabelStatistics::count).reversed())
                    .toList();

            boolean isSubTie = subStats.size() > 1 && (subStats.get(0).count() == subStats.get(1).count());

            if (!isSubTie) {
                return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_AND_DETAIL, targetMain, subStats.get(0).label(), ranks);
            }
        }

        return new MatchPreferenceAnalysis(PreferenceResultType.TYPE_ONLY, targetMain, null, ranks);
    }

    // 통계용 내부 레코드 (Java 16+)
    private record LabelStatistics(String label, int count) {}
}