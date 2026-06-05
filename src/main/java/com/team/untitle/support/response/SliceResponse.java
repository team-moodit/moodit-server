package com.team.untitle.support.response;

import java.util.List;

/**
 * 무한 스크롤을 위한 응답 객체
 * ApiResponse<SliceResponse<T>> 형태로 사용
 */
public record SliceResponse<T>(
        List<T> content,
        Boolean hasNext
) {
}
