package com.kleer.currency.util;

import com.kleer.currency.model.CurrencyPair;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility component for performing exchange rate calculations.
 * Handles rate inversions and cross-rate calculations with consistent precision.
 */
@Component
public class ExchangeRateCalculator {
    
    private static final int EXCHANGE_RATE_SCALE = 8;
    private static final RoundingMode EXCHANGE_RATE_ROUNDING = RoundingMode.HALF_UP;
    
    /**
     * Inverts an exchange rate.
     * For example, if EUR/SEK = 11.5, then SEK/EUR = 1/11.5 = 0.08695652
     * 
     * @param rate The original exchange rate
     * @return The inverted rate
     */
    public BigDecimal invert(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Rate cannot be null or zero");
        }
        return BigDecimal.ONE.divide(rate, EXCHANGE_RATE_SCALE, EXCHANGE_RATE_ROUNDING);
    }
    
    /**
     * Calculates a cross rate by dividing two exchange rates.
     * For example, to get EUR/USD from EUR/SEK and USD/SEK:
     * EUR/USD = USD/SEK ÷ EUR/SEK
     * 
     * @param numeratorRate The rate in the numerator
     * @param denominatorRate The rate in the denominator
     * @return The calculated cross rate
     */
    public BigDecimal calculateCrossRate(BigDecimal numeratorRate, BigDecimal denominatorRate) {
        if (numeratorRate == null || denominatorRate == null) {
            throw new IllegalArgumentException("Rates cannot be null");
        }
        if (denominatorRate.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Denominator rate cannot be zero");
        }
        return numeratorRate.divide(denominatorRate, EXCHANGE_RATE_SCALE, EXCHANGE_RATE_ROUNDING);
    }
    
    /**
     * Adds a currency pair and its rate to the map, along with its inverse.
     * 
     * @param rates The map to add rates to
     * @param pair The currency pair
     * @param rate The exchange rate
     */
    public void addRateWithInverse(Map<String, BigDecimal> rates, CurrencyPair pair, BigDecimal rate) {
        if (rate == null) {
            return;
        }
        rates.put(pair.toKey(), rate);
        rates.put(pair.inverse().toKey(), invert(rate));
    }
    
    /**
     * Calculates all possible exchange rates from base rates.
     * Given EUR/SEK and USD/SEK, calculates all six permutations including EUR/USD.
     * 
     * @param eurToSek EUR/SEK exchange rate
     * @param usdToSek USD/SEK exchange rate
     * @return Map of all calculated rates
     */
    public Map<String, BigDecimal> calculateAllRates(BigDecimal eurToSek, BigDecimal usdToSek) {
        Map<String, BigDecimal> rates = new HashMap<>();
        
        if (eurToSek != null) {
            addRateWithInverse(rates, new CurrencyPair("EUR", "SEK"), eurToSek);
        }
        
        if (usdToSek != null) {
            addRateWithInverse(rates, new CurrencyPair("USD", "SEK"), usdToSek);
        }
        
        if (eurToSek != null && usdToSek != null) {
            BigDecimal eurToUsd = calculateCrossRate(usdToSek, eurToSek);
            addRateWithInverse(rates, new CurrencyPair("EUR", "USD"), eurToUsd);
        }
        
        return rates;
    }
}

