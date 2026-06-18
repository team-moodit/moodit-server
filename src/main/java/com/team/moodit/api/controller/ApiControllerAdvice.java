package com.team.moodit.api.controller;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.ValueInstantiationException;

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof ValueInstantiationException valueInstantiationException) {
            if (valueInstantiationException.getCause() instanceof ApiException apiException) {
                log.info("[ApiException]: {}", apiException.getMessage(), apiException);
                return ResponseEntity.status(apiException.getErrorType().getStatus()).body(ApiResponse.error(apiException.getErrorType()));
            }
        }
        return ResponseEntity.status(ErrorType.INVALID_REQUEST.getStatus()).body(ApiResponse.error(ErrorType.INVALID_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("[Exception]: {}", e.getMessage(), e);
        return ResponseEntity.status(ErrorType.DEFAULT_ERROR.getStatus()).body(ApiResponse.error(ErrorType.DEFAULT_ERROR));
    }
}
