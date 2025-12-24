package com.tlback.scheduling.exception;

public class InvalidCronExpressionException extends RuntimeException {
    public InvalidCronExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCronExpressionException(String message) {
        super(message);
    }
}
