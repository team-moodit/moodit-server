package com.team.moodit.domain.review;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RateSummary {
    private BigDecimal rate;
    private long count;

    public static RateSummary EMPTY = new RateSummary(BigDecimal.ZERO, 0);
}
