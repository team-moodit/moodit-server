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

    private final Random random = new Random();

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
                .map(entry -> new LabelStatistics(
                        entry.getKey(),
                        entry.getValue().intValue()
                ))
                .sorted(Comparator.comparingInt(LabelStatistics::count).reversed())
                .toList();

        List<PreferenceRankDto> ranks = createRanks(mainStats);

        boolean isMainTie =
                mainStats.size() > 1
                        && mainStats.get(0).count() == mainStats.get(1).count();

        if (isMainTie) {
            MatchPreferenceAnalysis tieAnalysis = analyzeMainTie(
                    votedCandidates,
                    mainStats,
                    ranks
            );

            if (tieAnalysis != null) {
                return tieAnalysis;
            }

            return new MatchPreferenceAnalysis(
                    PreferenceResultType.TIE,
                    null,
                    null,
                    ranks
            );
        }

        String targetMain = mainStats.get(0).label();

        if (!hasDetailPreference(targetMain)) {
            return new MatchPreferenceAnalysis(
                    PreferenceResultType.TYPE_ONLY,
                    targetMain,
                    null,
                    ranks
            );
        }

        MatchPreferenceAnalysis detailAnalysis = analyzeSingleMainDetail(
                votedCandidates,
                targetMain,
                ranks
        );

        if (detailAnalysis != null) {
            return detailAnalysis;
        }

        return new MatchPreferenceAnalysis(
                PreferenceResultType.TYPE_ONLY,
                targetMain,
                null,
                ranks
        );
    }

    /**
     * 메인 선호가 동률인 경우 처리.
     *
     * 요구사항:
     * - 결과 타입은 TIE 상태를 유지한다.
     * - 단, 동률 선호 중 상세선호를 가진 선호가 포함되어 있으면
     *   해당 선호들의 preferenceDetail 선택 횟수를 집계한다.
     * - 가장 많이 선택된 상세선호를 미션 문구 기준으로 사용한다.
     * - 상세선호까지 동률이면 랜덤 선택한다.
     */
    private MatchPreferenceAnalysis analyzeMainTie(
            List<MatchVoteCandidateEntity> votedCandidates,
            List<LabelStatistics> mainStats,
            List<PreferenceRankDto> ranks
    ) {
        int topCount = mainStats.get(0).count();

        List<String> topMainPreferences = mainStats.stream()
                .filter(stat -> stat.count() == topCount)
                .map(LabelStatistics::label)
                .toList();

        List<String> detailSupportedPreferences = topMainPreferences.stream()
                .filter(this::hasDetailPreference)
                .toList();

        if (detailSupportedPreferences.isEmpty()) {
            return null;
        }

        return analyzeDetailTie(
                votedCandidates,
                detailSupportedPreferences,
                ranks
        );
    }

    /**
     * 메인 선호 동률 상태에서 상세선호 기준 미션 문구를 정하기 위한 분석.
     *
     * 주의:
     * 여기서 TYPE_AND_DETAIL로 반환하면 안 된다.
     * 최종 선호 결과는 여전히 TIE이기 때문에 PreferenceResultType.TIE를 유지한다.
     */
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

        Map<DetailKey, Long> detailCountMap = detailCandidates.stream()
                .collect(Collectors.groupingBy(
                        candidate -> new DetailKey(
                                candidate.getPreference(),
                                candidate.getPreferenceDetail()
                        ),
                        Collectors.counting()
                ));

        long maxDetailCount = detailCountMap.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        List<DetailStatistics> topDetails = detailCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxDetailCount)
                .map(entry -> new DetailStatistics(
                        entry.getKey().preference(),
                        entry.getKey().detail(),
                        entry.getValue().intValue()
                ))
                .toList();

        DetailStatistics selectedDetail = pickRandom(topDetails);

        if (selectedDetail == null) {
            return null;
        }

        return new MatchPreferenceAnalysis(
                PreferenceResultType.TIE,
                selectedDetail.preference(),
                selectedDetail.detail(),
                ranks
        );
    }

    /**
     * 메인 선호가 단독 1위이고, 그 선호가 상세선호를 가진 경우 처리.
     *
     * 기존 정책 유지:
     * - 상세선호가 단독 1위면 TYPE_AND_DETAIL
     * - 상세선호까지 동률이면 TYPE_ONLY
     */
    private MatchPreferenceAnalysis analyzeSingleMainDetail(
            List<MatchVoteCandidateEntity> votedCandidates,
            String targetMain,
            List<PreferenceRankDto> ranks
    ) {
        List<MatchVoteCandidateEntity> subFiltered = votedCandidates.stream()
                .filter(candidate -> targetMain.equals(candidate.getPreference()))
                .filter(candidate -> candidate.getPreferenceDetail() != null)
                .filter(candidate -> !candidate.getPreferenceDetail().isBlank())
                .toList();

        if (subFiltered.isEmpty()) {
            return null;
        }

        Map<String, Long> subCountMap = subFiltered.stream()
                .collect(Collectors.groupingBy(
                        MatchVoteCandidateEntity::getPreferenceDetail,
                        Collectors.counting()
                ));

        List<LabelStatistics> subStats = subCountMap.entrySet().stream()
                .map(entry -> new LabelStatistics(
                        entry.getKey(),
                        entry.getValue().intValue()
                ))
                .sorted(Comparator.comparingInt(LabelStatistics::count).reversed())
                .toList();

        boolean isSubTie =
                subStats.size() > 1
                        && subStats.get(0).count() == subStats.get(1).count();

        if (isSubTie) {
            return null;
        }

        return new MatchPreferenceAnalysis(
                PreferenceResultType.TYPE_AND_DETAIL,
                targetMain,
                subStats.get(0).label(),
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

    private boolean hasDetailPreference(String preference) {
        return PreferenceType.from(preference).hasDetail();
    }

    private DetailStatistics pickRandom(List<DetailStatistics> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }

        return details.get(random.nextInt(details.size()));
    }

    private record LabelStatistics(String label, int count) {
    }

    private record DetailKey(String preference, String detail) {
    }

    private record DetailStatistics(String preference, String detail, int count) {
    }
}