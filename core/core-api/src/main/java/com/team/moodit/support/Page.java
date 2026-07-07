package com.team.moodit.support;

import java.util.List;

public record Page<T>(
        List<T> content,
        long totalCount,
        boolean hasNext
) {
}
