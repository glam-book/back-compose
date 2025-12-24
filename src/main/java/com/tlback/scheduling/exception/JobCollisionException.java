package com.tlback.scheduling.exception;

public class JobCollisionException extends RuntimeException {

    public JobCollisionException(String msg) {
        super(msg);
    }
}
