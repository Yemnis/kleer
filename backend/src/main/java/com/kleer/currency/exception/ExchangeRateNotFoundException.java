package com.kleer.currency.exception;

/**
 * Exception thrown when an exchange rate is not found in the database.
 */
public class ExchangeRateNotFoundException extends RuntimeException {

    public ExchangeRateNotFoundException(String fromCurrency, String toCurrency) {
        super(String.format("Exchange rate not found for %s to %s", fromCurrency, toCurrency));
    }

    public ExchangeRateNotFoundException(String message) {
        super(message);
    }
}

