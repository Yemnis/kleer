package com.kleer.currency.repository;

import com.kleer.currency.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for ExchangeRate entity.
 * 
 * Provides database operations for managing exchange rates.
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Find exchange rate for a specific currency pair.
     *
     * @param fromCurrency Source currency code (e.g., "SEK")
     * @param toCurrency   Target currency code (e.g., "EUR")
     * @return Optional containing the exchange rate if found
     */
    Optional<ExchangeRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);

    /**
     * Check if an exchange rate exists for a currency pair.
     *
     * @param fromCurrency Source currency code
     * @param toCurrency   Target currency code
     * @return true if the rate exists, false otherwise
     */
    boolean existsByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);

    /**
     * Delete exchange rate for a specific currency pair.
     *
     * @param fromCurrency Source currency code
     * @param toCurrency   Target currency code
     */
    void deleteByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
}

