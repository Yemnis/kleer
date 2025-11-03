package com.kleer.currency.service;

import com.kleer.currency.dto.ConversionResponse;
import com.kleer.currency.entity.ExchangeRate;
import com.kleer.currency.exception.CurrencyNotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service for performing currency conversions.
 * 
 * Handles conversion logic with comprehensive validation using early returns.
 */
@Service
@Slf4j
public class CurrencyConversionService {

    private final ExchangeRateService exchangeRateService;

    private static final List<String> SUPPORTED_CURRENCIES = List.of("SEK", "EUR", "USD");
    private static final int DECIMAL_SCALE = 2;

    public CurrencyConversionService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * Convert an amount from one currency to another.
     * 
     * Uses early returns for all validation steps to improve readability
     * and maintainability.
     *
     * @param amount       Amount to convert
     * @param fromCurrency Source currency code
     * @param toCurrency   Target currency code
     * @return Conversion result with converted amount and rate
     */
    public ConversionResponse convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        log.debug("Converting {} {} to {}", amount, fromCurrency, toCurrency);

        // Early return for null amount
        if (amount == null) {
            log.warn("Conversion attempted with null amount");
            throw new IllegalArgumentException("Amount cannot be null");
        }

        // Early return for zero or negative amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Conversion attempted with invalid amount: {}", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // Early return for null or empty currencies
        if (fromCurrency == null || fromCurrency.trim().isEmpty()) {
            log.warn("Conversion attempted with null or empty source currency");
            throw new IllegalArgumentException("Source currency cannot be null or empty");
        }

        if (toCurrency == null || toCurrency.trim().isEmpty()) {
            log.warn("Conversion attempted with null or empty target currency");
            throw new IllegalArgumentException("Target currency cannot be null or empty");
        }

        // Normalize currencies to uppercase
        fromCurrency = fromCurrency.toUpperCase().trim();
        toCurrency = toCurrency.toUpperCase().trim();

        // Early return for unsupported source currency
        if (!isSupportedCurrency(fromCurrency)) {
            log.error("Unsupported source currency: {}", fromCurrency);
            throw new CurrencyNotSupportedException(fromCurrency);
        }

        // Early return for unsupported target currency
        if (!isSupportedCurrency(toCurrency)) {
            log.error("Unsupported target currency: {}", toCurrency);
            throw new CurrencyNotSupportedException(toCurrency);
        }

        // Early return for same currency conversion
        if (fromCurrency.equals(toCurrency)) {
            log.debug("Same currency conversion: {} to {}, returning original amount", 
                    fromCurrency, toCurrency);
            return ConversionResponse.builder()
                    .originalAmount(amount)
                    .convertedAmount(amount)
                    .rate(BigDecimal.ONE)
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .build();
        }

        // Get exchange rate from database
        ExchangeRate exchangeRate = exchangeRateService.getRate(fromCurrency, toCurrency);

        // Early return if rate is null (should not happen, but safety check)
        if (exchangeRate == null) {
            log.error("Exchange rate service returned null for {} to {}", fromCurrency, toCurrency);
            throw new IllegalStateException("Failed to retrieve exchange rate");
        }

        // Early return if rate value is null or invalid
        if (exchangeRate.getRate() == null || exchangeRate.getRate().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid exchange rate for {} to {}: {}", 
                    fromCurrency, toCurrency, exchangeRate.getRate());
            throw new IllegalStateException("Invalid exchange rate in database");
        }

        // Perform conversion
        BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate())
                .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

        log.info("Converted {} {} to {} {} (rate: {})", 
                amount, fromCurrency, convertedAmount, toCurrency, exchangeRate.getRate());

        return ConversionResponse.builder()
                .originalAmount(amount)
                .convertedAmount(convertedAmount)
                .rate(exchangeRate.getRate())
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .build();
    }

    /**
     * Check if a currency is supported.
     * 
     * @param currency Currency code to check
     * @return true if supported, false otherwise
     */
    private boolean isSupportedCurrency(String currency) {
        // Early return for null or empty
        if (currency == null || currency.isEmpty()) {
            return false;
        }

        return SUPPORTED_CURRENCIES.contains(currency.toUpperCase());
    }

    /**
     * Get list of supported currencies.
     *
     * @return List of supported currency codes
     */
    public List<String> getSupportedCurrencies() {
        return List.copyOf(SUPPORTED_CURRENCIES);
    }
}

