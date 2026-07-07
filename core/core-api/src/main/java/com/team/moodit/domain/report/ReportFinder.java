package com.team.moodit.domain.report;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.storage.db.core.MatchPreferenceResultRepository;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.storage.db.core.PreferenceSelectionCountProjection;
import com.team.moodit.storage.db.core.UserMissionRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportFinder {
    private final MatchResultRepository matchResultRepository;
    private final MatchPreferenceResultRepository matchPreferenceResultRepository;
    private final UserMissionRepository userMissionRepository;

    public UserTasteReport find(Long userId) {
        long totalMatchCount = matchResultRepository.countByUserId(userId);
        long reviewedMissionCount = userMissionRepository.countByUserIdAndStateAndStatus(
                userId,
                UserMissionState.REVIEWED,
                EntityStatus.ACTIVE
        );

        List<PreferenceSelectionCountProjection> result = matchPreferenceResultRepository.countPreferenceSelectionByUserId(
                userId);

        Map<PreferenceType, Long> selectionCountMap = result.stream().collect(Collectors.toMap(
                PreferenceSelectionCountProjection::getPreferenceType,
                PreferenceSelectionCountProjection::getCount
        ));
        long totalSelectionCount = selectionCountMap.values().stream().mapToLong(Long::longValue).sum();

        List<PreferenceCriterionShare> criteria = Arrays.stream(PreferenceType.values()).map(type -> {
            long selectionCount = selectionCountMap.getOrDefault(type, 0L);
            int percentage = calculatePercentage(selectionCount, totalSelectionCount);

            return new PreferenceCriterionShare(
                    type,
                    selectionCount,
                    percentage
            );
        }).toList();

        return new UserTasteReport(
                new AnalysisRecordSummary(
                        totalMatchCount,
                        reviewedMissionCount
                ),
                new PreferenceReport(
                        totalMatchCount,
                        criteria
                )
        );
    }

    private int calculatePercentage(long part, long whole) {
        if (whole == 0) return 0;
        return (int) Math.round(part * 100.0 / whole);
    }
}
