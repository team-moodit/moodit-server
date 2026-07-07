package com.team.moodit.domain.report;

import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportFinder reportFinder;

    public UserTasteReport getReport(ApiUser apiUser) {
        return reportFinder.find(apiUser.getId());
    }
}
