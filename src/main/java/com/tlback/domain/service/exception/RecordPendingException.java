package com.tlback.domain.service.exception;

public class RecordPendingException extends RuntimeException {
    public RecordPendingException(String message) {
        super(message);
    }

    public RecordPendingException(String message, Throwable ex) {
        super(message, ex);
    }
}
