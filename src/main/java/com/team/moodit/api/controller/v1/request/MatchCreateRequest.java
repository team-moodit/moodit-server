package com.team.moodit.api.controller.v1.request;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;

import java.util.List;

public record MatchCreateRequest(
   String title,
   List<Long> images
) {
    public MatchCreateRequest {
        // 제목 검증
        if (title == null || title.isBlank()) {
            throw new ApiException(ErrorType.MISSING_TITLE);
        }

        if(title.length() > 15){
            throw new ApiException(ErrorType.INVALID_TITLE);
        }

        //이미지 개수 검증 (요구사항: 8장~32장)
        if (images == null || images.size() < 8 || images.size() > 32) {
            throw new ApiException(ErrorType.INVALID_IMAGE_COUNT);
        }

    }

}

