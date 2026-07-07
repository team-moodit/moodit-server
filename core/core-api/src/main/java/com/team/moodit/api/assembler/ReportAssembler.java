package com.team.moodit.api.assembler;

import com.team.moodit.api.controller.v1.response.ReportResponse;
import com.team.moodit.domain.report.ReportService;
import com.team.moodit.domain.report.UserTasteReport;
import com.team.moodit.domain.review.RateSummary;
import com.team.moodit.domain.review.ReviewService;
import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportAssembler {
    private final ReportService reportService;
    private final ReviewService reviewService;

    public ReportResponse getReport(ApiUser apiUser) {
        UserTasteReport report = reportService.getReport(apiUser);
        RateSummary rateSummary = reviewService.findRateSummary(apiUser);

        return ReportResponse.of(report, rateSummary);
    }
}
