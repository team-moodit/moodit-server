package com.team.moodit.support;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record OffsetLimit(
        Integer offset,
        Integer limit
) {
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 10;

    public OffsetLimit {
        offset = offset == null ? DEFAULT_OFFSET : offset;
        limit = limit == null ? DEFAULT_LIMIT : limit;

        if (offset < 0) throw new ApiException(ErrorType.INVALID_REQUEST);
        if (limit < 1) throw new ApiException(ErrorType.INVALID_REQUEST);
    }

    public Pageable toPageable() {
        return PageRequest.of(offset / limit, limit);
    }
}
