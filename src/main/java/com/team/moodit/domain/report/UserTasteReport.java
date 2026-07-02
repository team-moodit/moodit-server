package com.team.moodit.domain.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserTasteReport {
    AnalysisRecordSummary recordSummary;
    PreferenceReport preferenceReport;
    MissionSatisfactionSummary satisfactionSummary;
}
