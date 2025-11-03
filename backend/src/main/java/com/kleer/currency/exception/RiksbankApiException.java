package com.kleer.currency.exception;

/**
 * Exception thrown when there's an error communicating with Riksbank API.
 */
public class RiksbankApiException extends RuntimeException {

    public RiksbankApiException(String message) {
        super(message);
    }

    public RiksbankApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

