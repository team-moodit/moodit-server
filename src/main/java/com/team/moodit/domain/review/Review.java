package com.team.moodit.domain.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Review {
    private Long id;
    private Long userId;
    private ReviewTarget target;
    private ReviewContent content;
}
