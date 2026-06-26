package com.team.moodit.domain.match;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.Getter;

@Getter
public class VoteCommand {
    private final Long photoId;
    private final Long reasonId;

    public VoteCommand(Long photoId, Long reasonId) {
        if (photoId == null || reasonId == null) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
        this.photoId = photoId;
        this.reasonId = reasonId;
    }
}
