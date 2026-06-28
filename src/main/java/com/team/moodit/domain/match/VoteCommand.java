package com.team.moodit.domain.match;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.Getter;

@Getter
public class VoteCommand {
    private final Long matchUpId; // 1. 경기 ID 추가
    private final Long photoId;
    private final Long reasonId;

    // 2. 생성자 파라미터에 matchUpId 추가 및 검증 로직 반영
    public VoteCommand(Long matchUpId, Long photoId, Long reasonId) {
        if (matchUpId == null || photoId == null || reasonId == null) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
        this.matchUpId = matchUpId;
        this.photoId = photoId;
        this.reasonId = reasonId;
    }
}