package com.team.moodit.api.controller.v1.response;

import static com.team.moodit.api.controller.v1.request.AddReviewRequest.RATE_MAX;
import static com.team.moodit.api.controller.v1.request.AddReviewRequest.RATE_MIN;

import com.team.moodit.api.controller.v1.response.PreferenceReportResponse.PreferenceDistribution;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.domain.report.PreferenceCriterionShare;
import com.team.moodit.domain.report.UserTasteReport;
import com.team.moodit.domain.review.RateSummary;
import java.util.List;

public record ReportResponse(
        ReportSummaryResponse summary,
        PreferenceReportResponse preferenceReport,
        RateSummaryResponse rateSummary
) {
    public static ReportResponse of(
            UserTasteReport report,
            RateSummary rateSummary
    ) {
        return new ReportResponse(
                new ReportSummaryResponse(
                        report.getRecordSummary().getTotalMatchCount(),
                        report.getRecordSummary().getReviewedMissionCount()
                ),
                new PreferenceReportResponse(
                        report.getPreferenceReport().getTotalMatchCount(),
                        PreferenceDistribution.of(report.getPreferenceReport().topCriterion()),
                        report.getPreferenceReport().getCriteria().stream()
                                .filter(criteria -> criteria.getPercentage() > 0)
                                .map(PreferenceDistribution::of)
                                .toList()
                ),
                new RateSummaryResponse(
                        rateSummary.getCount(),
                        rateSummary.getRate(),
                        RATE_MIN,
                        RATE_MAX

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
        long totalMatchCount,
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
