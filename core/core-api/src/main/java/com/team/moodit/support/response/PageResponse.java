package com.team.moodit.support.response;

import com.team.moodit.support.Page;
import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long totalCount,
        boolean hasNext
) {
    public static <T> PageResponse<T> of(
            Page<T> page
    ) {
        return new PageResponse<>(
                page.content(),
                page.totalCount(),
                page.hasNext()
        );
    }
}
