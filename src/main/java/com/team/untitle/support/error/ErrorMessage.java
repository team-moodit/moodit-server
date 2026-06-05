package com.team.untitle.support.error;

public record ErrorMessage(
        String code,
        String message
) {
    public ErrorMessage(ErrorType errorType) {
        this(errorType.name(), errorType.getMessage());
    }
}
