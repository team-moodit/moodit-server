package com.team.moodit.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다.", LogLevel.INFO),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 데이터를 찾을 수 없습니다.", LogLevel.INFO),

    // Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", LogLevel.INFO),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰 입니다.", LogLevel.INFO),

    // File
    FILE_UPLOADING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),

    //Image
    INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "이미지는 8장에서 32장 사이여야 합니다.", LogLevel.INFO),

    //Title
    MISSING_TITLE(HttpStatus.BAD_REQUEST,"제목 입력은 필수입니다.",LogLevel.INFO),
    INVALID_TITLE(HttpStatus.BAD_REQUEST,"제목은 1자 이상 20자 이하로 입력해주세요.",LogLevel.INFO);

    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;
}
