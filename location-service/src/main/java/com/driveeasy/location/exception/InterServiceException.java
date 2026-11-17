package com.driveeasy.location.exception;

public class InterServiceException extends RuntimeException {
    public InterServiceException(String message) {
        super(message);
    }

    public InterServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
