package com.kleer.currency.model;

/**
 * Represents a type-safe currency pair for exchange rate operations.
 * 
 * @param from The source currency code (e.g., "EUR")
 * @param to The target currency code (e.g., "SEK")
 */
public record CurrencyPair(String from, String to) {
    
    /**
     * Creates a currency pair with validation.
     */
    public CurrencyPair {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("Source currency cannot be null or blank");
        }
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Target currency cannot be null or blank");
        }
        from = from.toUpperCase();
        to = to.toUpperCase();
    }
    
    /**
     * Converts the currency pair to a string key format (e.g., "EUR/SEK").
     * 
     * @return The currency pair as a formatted string
     */
    public String toKey() {
        return from + "/" + to;
    }
    
    /**
     * Creates an inverted currency pair (e.g., EUR/SEK becomes SEK/EUR).
     * 
     * @return The inverted currency pair
     */
    public CurrencyPair inverse() {
        return new CurrencyPair(to, from);
    }
    
    /**
     * Creates a CurrencyPair from a string key (e.g., "EUR/SEK").
     * 
     * @param key The currency pair key
     * @return The CurrencyPair instance
     * @throws IllegalArgumentException if the key format is invalid
     */
    public static CurrencyPair fromKey(String key) {
        if (key == null || !key.contains("/")) {
            throw new IllegalArgumentException("Invalid currency pair key: " + key);
        }
        String[] parts = key.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid currency pair key: " + key);
        }
        return new CurrencyPair(parts[0], parts[1]);
    }
}

