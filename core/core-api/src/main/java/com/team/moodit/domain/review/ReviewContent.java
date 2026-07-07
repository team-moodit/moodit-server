package com.team.moodit.domain.review;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewContent {
    private BigDecimal rate;
    private String content;
}
