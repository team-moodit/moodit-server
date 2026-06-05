package com.team.untitle.api.controller;

import com.team.untitle.support.error.ApiException;
import com.team.untitle.support.error.ErrorType;
import com.team.untitle.support.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(ApiException e) {
        switch (e.getErrorType().getLogLevel()) {
            case LogLevel.ERROR -> log.error("[ApiException]: {}", e.getMessage(), e);
            case LogLevel.WARN -> log.warn("[ApiException]: {}", e.getMessage(), e);
            default -> log.info("[ApiException]: {}", e.getMessage(), e);
        }
        return ResponseEntity.status(e.getErrorType().getStatus()).body(ApiResponse.error(e.getErrorType()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("[Exception]: {}", e.getMessage(), e);
        return ResponseEntity.status(ErrorType.DEFAULT_ERROR.getStatus()).body(ApiResponse.error(ErrorType.DEFAULT_ERROR));
    }
}
