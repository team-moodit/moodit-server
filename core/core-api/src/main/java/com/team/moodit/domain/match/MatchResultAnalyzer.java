package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceDetailType;
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
        // 투표 데이터가 없으면 선호 결과를 계산할 수 없으므로 TIE 반환
        if (votedCandidates == null || votedCandidates.isEmpty()) {
            return new MatchPreferenceAnalysis(
                    PreferenceResultType.TIE,
                    null,
                    null,
                    List.of()
            );
        }
        // 메인 선호(FITNESS, TREND, CONSISTENCE, AESTHETICS)별 선택 횟수 집계
        Map<String, Long> mainCountMap = votedCandidates.stream()
                .collect(Collectors.groupingBy(
                        MatchVoteCandidateEntity::getPreference,
                        Collectors.counting()
                ));
        // 선택 횟수 기준 내림차순 정렬
        List<LabelStatistics> mainStats = mainCountMap.entrySet().stream()
                .map(entry -> new LabelStatistics(
                        entry.getKey(),
                        entry.getValue().intValue()
                ))
                .sorted(Comparator.comparingInt(LabelStatistics::count).reversed())
                .toList();
        // 메인 선호 순위 생성
        // preferenceDetailType은 기본적으로 null이며,
        // TIE인 경우에만 상세선호를 추가로 채운다.
        List<PreferenceRankDto> ranks = createRanks(mainStats);
        // 메인 선호 1위가 동률인지 확인
        boolean isMainTie =
                mainStats.size() > 1
                        && mainStats.get(0).count() == mainStats.get(1).count();
        /*
         * PM 요구사항
         *
         * 메인 선호가 동률이면 결과 타입은 TIE를 유지한다.
         *
         * 단,
         * FITNESS / AESTHETICS 처럼 상세선호를 가진 타입은
         * 각 타입별 상세선호 1위를 계산하여 저장한다.
         *
         * 이후 미션 생성 시
         * FITNESS -> BODY_FIT
         * AESTHETICS -> COLOR
         * 처럼 각각의 상세선호를 이용하여 미션을 생성한다.
         *
         * TREND / CONSISTENCE 는 상세선호가 없으므로
         * preferenceDetailType은 null로 유지한다.
         */
        if (isMainTie) {
            List<PreferenceRankDto> tieRanks = applyTiePreferenceDetails(
                    votedCandidates,
                    ranks
            );

            return new MatchPreferenceAnalysis(
                    PreferenceResultType.TIE,
                    null,
                    null,
                    tieRanks
            );
        }

        String targetMain = mainStats.get(0).label();
        // 상세선호가 없는 타입(TREND, CONSISTENCE)은 TYPE_ONLY 반환
        if (!hasDetailPreference(targetMain)) {
            return new MatchPreferenceAnalysis(
                    PreferenceResultType.TYPE_ONLY,
                    targetMain,
                    null,
                    ranks
            );
        }
        // 상세선호가 존재하는 타입(FITNESS, AESTHETICS)은 상세선호 분석
        MatchPreferenceAnalysis detailAnalysis = analyzeSingleMainDetail(
                votedCandidates,
                targetMain,
                ranks
        );
        // 상세선호까지 단독 1위인 경우 TYPE_AND_DETAIL 반환
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
     * 메인 선호가 단독 1위이고,
     * 상세선호를 지원하는 타입(FITNESS, AESTHETICS)인 경우
     * 상세선호까지 분석한다.
     *
     * 기존 정책 유지
     * - 상세선호 단독 1위 -> TYPE_AND_DETAIL
     * - 상세선호 동률 -> TYPE_ONLY
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
    /**
     * 메인 선호 순위를 생성한다.
     *
     * preferenceDetailType은 기본적으로 null이며,
     * TIE인 경우 applyTiePreferenceDetails()에서
     * rank=1인 선호들만 상세선호를 채운다.
     */
    private List<PreferenceRankDto> createRanks(List<LabelStatistics> mainStats) {
        List<PreferenceRankDto> ranks = new ArrayList<>();

        int rank = 1;

        for (int i = 0; i < mainStats.size(); i++) {
            if (i > 0 && mainStats.get(i).count() != mainStats.get(i - 1).count()) {
                rank++;
            }

            ranks.add(new PreferenceRankDto(
                    mainStats.get(i).label(),
                    null,
                    rank,
                    mainStats.get(i).count()
            ));
        }

        return ranks;
    }
    /**
     * PM 요구사항
     *
     * 메인 선호가 동률일 경우
     * rank=1인 선호들 중
     * 상세선호를 지원하는 타입(FITNESS, AESTHETICS)에 대해서만
     * 각 타입별 상세선호 1위를 계산하여 저장한다.
     *
     * TREND, CONSISTENCE는
     * 상세선호가 없으므로 null을 유지한다.
     */
    private List<PreferenceRankDto> applyTiePreferenceDetails(
            List<MatchVoteCandidateEntity> votedCandidates,
            List<PreferenceRankDto> ranks
    ) {
        return ranks.stream()
                .map(rank -> {
                    if (!hasDetailPreference(rank.getLabel())) {
                        return rank;
                    }
                    PreferenceDetailType detail = findTopPreferenceDetail(
                            votedCandidates,
                            rank.getLabel()
                    );

                    return new PreferenceRankDto(
                            rank.getLabel(),
                            detail,
                            rank.getRank(),
                            rank.getCount()
                    );
                })
                .toList();
    }
    /**
     * 특정 메인 선호 안에서
     * 가장 많이 선택된 상세선호를 반환한다.
     *
     * 상세선호까지 동률인 경우
     * 기존 정책과 동일하게 랜덤으로 하나를 선택한다.
     */
    private PreferenceDetailType findTopPreferenceDetail(
            List<MatchVoteCandidateEntity> votedCandidates,
            String preference
    ) {
        Map<String, Long> detailCountMap = votedCandidates.stream()
                .filter(candidate -> preference.equals(candidate.getPreference()))
                .filter(candidate -> candidate.getPreferenceDetail() != null)
                .filter(candidate -> !candidate.getPreferenceDetail().isBlank())
                .collect(Collectors.groupingBy(
                        MatchVoteCandidateEntity::getPreferenceDetail,
                        Collectors.counting()
                ));

        if (detailCountMap.isEmpty()) {
            return null;
        }

        long maxCount = detailCountMap.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        List<String> topDetails = detailCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .toList();

        String selectedDetail = topDetails.get(random.nextInt(topDetails.size()));

        return PreferenceDetailType.from(selectedDetail);
    }

    /**
     * 해당 메인 선호가
     * 상세선호를 지원하는 타입인지 확인한다.
     */
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