package com.team.untitle.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다.", LogLevel.INFO),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 데이터를 찾을 수 없습니다.", LogLevel.INFO);

    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;
}
