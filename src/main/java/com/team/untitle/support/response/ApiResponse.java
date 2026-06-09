package com.team.untitle.support.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.team.untitle.support.error.ErrorMessage;
import com.team.untitle.support.error.ErrorType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T success,
        ErrorMessage error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(null, null);
    }

    public static <T> ApiResponse<T> error(ErrorType errorType) {
        return new ApiResponse<>(null, new ErrorMessage(errorType));
    }
}
