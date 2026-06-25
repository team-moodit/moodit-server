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
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 요청입니다.", LogLevel.INFO),

    // Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", LogLevel.INFO),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰 입니다.", LogLevel.INFO),

    // Match Result
    NOT_FOUND_MATCH_RESULT(HttpStatus.NOT_FOUND, "매치 결과를 찾을 수 없습니다.", LogLevel.INFO),

    // File
    FILE_UPLOADING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),

    // Image
    INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "이미지는 8장에서 32장 사이여야 합니다.", LogLevel.INFO),

    // Title
    MISSING_TITLE(HttpStatus.BAD_REQUEST,"제목은 필수입니다.", LogLevel.INFO),
    INVALID_TITLE(HttpStatus.BAD_REQUEST,"제목은 1자 이상 15자 이하입니다.", LogLevel.INFO),

    //MatchUp
    INVALID_MATCH_UP_CANDIDATE(HttpStatus.BAD_REQUEST,"대결 참가자가 존재하지 않습니다.",LogLevel.INFO),
    INVALID_MATCH_UP_SAME_CANDIDATE(HttpStatus.BAD_REQUEST,"동일한 참가자가 대결할 수 없습니다.",LogLevel.INFO),
    INVALID_MATCH_UP_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 매치업 타입입니다.", LogLevel.ERROR);

    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;
}
