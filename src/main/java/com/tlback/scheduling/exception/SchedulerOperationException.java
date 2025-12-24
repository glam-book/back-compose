package com.tlback.scheduling.exception;

public class SchedulerOperationException extends RuntimeException {
    public SchedulerOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulerOperationException(Throwable cause) {
        super(cause);
    }
}
