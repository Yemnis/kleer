package com.kleer.currency.exception;

/**
 * Exception thrown when an unsupported currency is requested.
 */
public class CurrencyNotSupportedException extends RuntimeException {

    public CurrencyNotSupportedException(String currency) {
        super(String.format("Currency '%s' is not supported. Supported currencies: SEK, EUR, USD", currency));
    }
}

