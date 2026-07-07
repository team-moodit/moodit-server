package com.team.moodit.support;

import java.util.List;

/**
 * JPA 객체로부터 레이어 격리를 위한 Slice 객체
 */
public record Slice<T>(
        List<T> content,
        Boolean hasNext
) {
}
