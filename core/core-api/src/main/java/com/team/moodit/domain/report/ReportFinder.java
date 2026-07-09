package com.team.moodit.domain.report;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.storage.db.core.MatchChoiceRepository;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.storage.db.core.PreferenceDetailVoteCountProjection;
import com.team.moodit.storage.db.core.PreferenceVoteCountProjection;
import com.team.moodit.storage.db.core.UserMissionRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportFinder {
    private static final int DISTRIBUTION_LIMIT = 3;
    private static final List<PreferenceType> PREFERENCE_DISPLAY_ORDER = List.of(
            PreferenceType.AESTHETICS,
            PreferenceType.FITNESS,
            PreferenceType.TREND,
            PreferenceType.CONSISTENCE
    );
    private static final Map<PreferenceType, List<PreferenceDetailType>> DETAIL_DISPLAY_ORDER = Map.of(
            PreferenceType.FITNESS,
            List.of(
                    PreferenceDetailType.BODY_FIT,
                    PreferenceDetailType.VIBE,
                    PreferenceDetailType.MATCHABLE
            ),
            PreferenceType.AESTHETICS,
            List.of(
                    PreferenceDetailType.DESIGN,
                    PreferenceDetailType.MOOD,
                    PreferenceDetailType.COLOR
            )
    );

    private final MatchResultRepository matchResultRepository;
    private final MatchChoiceRepository matchChoiceRepository;
    private final UserMissionRepository userMissionRepository;

    public UserTasteReport find(Long userId) {
        long totalMatchCount = matchResultRepository.countByUserId(userId);
        long reviewedMissionCount = userMissionRepository.countByUserIdAndStateAndStatus(
                userId,
                UserMissionState.REVIEWED,
                EntityStatus.ACTIVE
        );

        List<PreferenceCriterionShare> preferenceShares = findPreferenceShares(userId);
        PreferenceCriterionShare topPreference = findSingleTop(preferenceShares);
        PreferenceReport preferenceReport = createPreferenceReport(
                userId,
                totalMatchCount,
                preferenceShares,
                topPreference
        );

        return new UserTasteReport(
                new AnalysisRecordSummary(
                        totalMatchCount,
                        reviewedMissionCount
                ),
                preferenceReport
        );
    }

    private PreferenceReport createPreferenceReport(
            Long userId,
            long totalMatchCount,
            List<PreferenceCriterionShare> preferenceShares,
            PreferenceCriterionShare topPreference
    ) {
        if (preferenceShares.stream().allMatch(criteria -> criteria.getSelectedCount() == 0)) {
            return new PreferenceReport(
                    totalMatchCount,
                    PreferenceReportType.NONE,
                    null,
                    null,
                    List.of()
            );
        }

        if (topPreference == null) {
            return new PreferenceReport(
                    totalMatchCount,
                    PreferenceReportType.PREFERENCE_TIE,
                    null,
                    null,
                    topThree(preferenceShares, this::preferenceDisplayOrder)
            );
        }

        if (!topPreference.getType().hasDetail()) {
            return new PreferenceReport(
                    totalMatchCount,
                    PreferenceReportType.PREFERENCE_ONLY,
                    topPreference,
                    null,
                    topThree(preferenceShares, this::preferenceDisplayOrder)
            );
        }

        List<PreferenceCriterionShare> detailShares = findDetailShares(
                userId,
                topPreference.getType()
        );
        PreferenceCriterionShare topPreferenceDetail = findSingleTop(detailShares);

        if (topPreferenceDetail == null) {
            return new PreferenceReport(
                    totalMatchCount,
                    PreferenceReportType.PREFERENCE_ONLY,
                    topPreference,
                    null,
                    topThree(preferenceShares, this::preferenceDisplayOrder)
            );
        }

        return new PreferenceReport(
                totalMatchCount,
                PreferenceReportType.PREFERENCE_DETAIL,
                topPreference,
                topPreferenceDetail,
                topThree(detailShares, this::detailDisplayOrder)
        );
    }

    private List<PreferenceCriterionShare> findPreferenceShares(Long userId) {
        Map<PreferenceType, Long> selectionCountMap = matchChoiceRepository.countVotedPreferenceByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                        projection -> PreferenceType.from(projection.getPreference()),
                        PreferenceVoteCountProjection::getCount
                ));

        long totalSelectionCount = selectionCountMap.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return PREFERENCE_DISPLAY_ORDER.stream()
                .map(type -> {
                    long selectionCount = selectionCountMap.getOrDefault(type, 0L);
                    int percentage = calculatePercentage(selectionCount, totalSelectionCount);
                    return PreferenceCriterionShare.preference(
                            type,
                            selectionCount,
                            percentage
                    );
                })
                .toList();
    }

    private List<PreferenceCriterionShare> findDetailShares(
            Long userId,
            PreferenceType preferenceType
    ) {
        Map<PreferenceDetailType, Long> detailCountMap = matchChoiceRepository
                .countVotedPreferenceDetailByUserIdAndPreference(userId, preferenceType.name())
                .stream()
                .collect(Collectors.toMap(
                        projection -> PreferenceDetailType.from(projection.getPreferenceDetail()),
                        PreferenceDetailVoteCountProjection::getCount
                ));

        long totalDetailCount = detailCountMap.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return DETAIL_DISPLAY_ORDER.getOrDefault(preferenceType, List.of()).stream()
                .map(detailType -> {
                    long selectionCount = detailCountMap.getOrDefault(detailType, 0L);
                    int percentage = calculatePercentage(selectionCount, totalDetailCount);
                    return PreferenceCriterionShare.detail(
                            preferenceType,
                            detailType,
                            selectionCount,
                            percentage
                    );
                })
                .toList();
    }

    private PreferenceCriterionShare findSingleTop(List<PreferenceCriterionShare> criteria) {
        long maxSelectedCount = criteria.stream()
                .mapToLong(PreferenceCriterionShare::getSelectedCount)
                .max()
                .orElse(0);

        if (maxSelectedCount == 0) {
            return null;
        }

        List<PreferenceCriterionShare> topCriteria = criteria.stream()
                .filter(criteriaShare -> criteriaShare.getSelectedCount() == maxSelectedCount)
                .toList();

        if (topCriteria.size() != 1) {
            return null;
        }

        return topCriteria.get(0);
    }

    private List<PreferenceCriterionShare> topThree(
            List<PreferenceCriterionShare> criteria,
            ToIntFunction<PreferenceCriterionShare> displayOrder
    ) {
        return criteria.stream()
                .sorted(Comparator.comparingLong(PreferenceCriterionShare::getSelectedCount)
                        .reversed()
                        .thenComparingInt(displayOrder))
                .limit(DISTRIBUTION_LIMIT)
                .toList();
    }

    private int preferenceDisplayOrder(PreferenceCriterionShare criteria) {
        return PREFERENCE_DISPLAY_ORDER.indexOf(criteria.getType());
    }

    private int detailDisplayOrder(PreferenceCriterionShare criteria) {
        return DETAIL_DISPLAY_ORDER.getOrDefault(criteria.getType(), List.of())
                .indexOf(criteria.getDetailType());
    }

    private int calculatePercentage(long part, long whole) {
        if (whole == 0) return 0;
        return (int) Math.round(part * 100.0 / whole);
    }
}
