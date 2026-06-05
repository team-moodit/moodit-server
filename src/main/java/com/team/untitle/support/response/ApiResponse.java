package com.team.untitle.support.response;

import com.team.untitle.support.error.ErrorMessage;
import com.team.untitle.support.error.ErrorType;

public record ApiResponse<T>(
        T success,
        ErrorMessage error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> error(ErrorType errorType) {
        return new ApiResponse<>(null, new ErrorMessage(errorType));
    }
}
