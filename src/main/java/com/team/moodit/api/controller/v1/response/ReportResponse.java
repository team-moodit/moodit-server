package com.team.moodit.api.controller.v1.response;

import com.team.moodit.api.controller.v1.response.PreferenceReportResponse.PreferenceDistribution;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.domain.feedback.FeedbackSpec;
import com.team.moodit.domain.report.PreferenceCriterionShare;
import com.team.moodit.domain.report.UserTasteReport;
import java.util.List;

public record ReportResponse(
        ReportSummaryResponse summary,
        PreferenceReportResponse preferenceReport,
        MissionSatisfactionResponse missionSatisfaction
) {
    public static ReportResponse of(UserTasteReport report) {
        return new ReportResponse(
                new ReportSummaryResponse(
                        report.getRecordSummary().getTotalMatchCount(),
                        report.getRecordSummary().getCompletedMissionCount()
                ),
                new PreferenceReportResponse(
                        report.getPreferenceReport().getTotalSelectionCount(),
                        PreferenceDistribution.of(report.getPreferenceReport().topCriterion()),
                        report.getPreferenceReport().getCriteria().stream().map(PreferenceDistribution::of).toList()
                ),
                new MissionSatisfactionResponse(
                        report.getSatisfactionSummary().getFeedbackCount(),
                        report.getSatisfactionSummary().getAverageScore(),
                        FeedbackSpec.MIN_SATISFACTION_SCORE,
                        FeedbackSpec.MAX_SATISFACTION_SCORE

                )
        );
    }
}

record ReportSummaryResponse(
        long totalMatchCount,
        long completedMissionCount
) {
}

record PreferenceReportResponse(
        long totalSelectionCount,
        PreferenceDistribution topPreference,
        List<PreferenceDistribution> distributions

) {
    record PreferenceDistribution(
            PreferenceType type,
            String title,
            long selectedCount,
            int percentage
    ) {
        public static PreferenceDistribution of(PreferenceCriterionShare criteria) {
            return new PreferenceDistribution(
                    criteria.getType(),
                    criteria.getType().getTitle(),
                    criteria.getSelectedCount(),
                    criteria.getPercentage()
            );
        }
    }
}

record MissionSatisfactionResponse(
        long feedbackCount,
        double averageScore,
        double minScore,
        double maxScore
) {}
