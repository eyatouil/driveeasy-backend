package com.driveeasy.vehicule.exception;

/** Levée quand un service appelé (location-service) est injoignable : HTTP 503. */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
