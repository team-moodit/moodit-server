package com.team.moodit.api.controller.v1.response;

import java.math.BigDecimal;

public record RateSummaryResponse(
        long count,
        BigDecimal rate,
        BigDecimal minRate,
        BigDecimal maxRate
) {
}
