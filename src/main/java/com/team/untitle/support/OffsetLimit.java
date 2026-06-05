package com.team.untitle.support;

import com.team.untitle.support.error.ApiException;
import com.team.untitle.support.error.ErrorType;

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
}
