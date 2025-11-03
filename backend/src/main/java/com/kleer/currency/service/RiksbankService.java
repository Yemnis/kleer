package com.kleer.currency.service;

import com.kleer.currency.dto.riksbank.RiksbankObservation;
import com.kleer.currency.exception.RiksbankApiException;
import com.kleer.currency.util.ExchangeRateCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for fetching exchange rates from the Riksbank API.
 * Handles API communication and delegates rate calculations to ExchangeRateCalculator.
 */
@Service
@Slf4j
public class RiksbankService {

    private final RestTemplate restTemplate;
    private final String riksbankBaseUrl;
    private final ExchangeRateCalculator rateCalculator;

    // Business constants
    private static final int DAYS_LOOKBACK = 7;
    private static final List<String> SUPPORTED_CURRENCIES = List.of("SEK", "EUR", "USD");
    
    // API constants
    private static final String OBSERVATIONS_ENDPOINT = "/Observations";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Map<String, String> CURRENCY_SERIES = Map.of(
            "SEKEUR", "SEKEURPMI",
            "SEKUSD", "SEKUSDPMI"
    );

    public RiksbankService(
            RestTemplate restTemplate,
            @Value("${riksbank.api.base-url}") String riksbankBaseUrl,
            ExchangeRateCalculator rateCalculator) {
        this.restTemplate = restTemplate;
        this.riksbankBaseUrl = riksbankBaseUrl;
        this.rateCalculator = rateCalculator;
    }

    /**
     * Fetches the latest exchange rates from Riksbank API.
     * Returns rates for all supported currency pairs (EUR, USD, SEK).
     * 
     * @return Map of currency pairs to exchange rates (e.g., "EUR/SEK" -> rate)
     * @throws RiksbankApiException if no rates could be fetched
     */
    public Map<String, BigDecimal> fetchLatestRates() {
        log.info("Fetching latest exchange rates from Riksbank API");

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(DAYS_LOOKBACK);
        String fromDate = startDate.format(DATE_FORMATTER);
        String toDate = today.format(DATE_FORMATTER);

        // Fetch base rates from Riksbank API
        // NOTE: Riksbank returns EUR/SEK and USD/SEK (how many SEK per foreign currency)
        BigDecimal eurToSek = fetchCurrencyRate("SEKEUR", fromDate, toDate);
        BigDecimal usdToSek = fetchCurrencyRate("SEKUSD", fromDate, toDate);

        // Calculate all rates including inversions and cross-rates
        Map<String, BigDecimal> rates = rateCalculator.calculateAllRates(eurToSek, usdToSek);

        if (rates.isEmpty()) {
            log.error("No valid exchange rates fetched from Riksbank API");
            throw new RiksbankApiException("Failed to fetch exchange rates from Riksbank API");
        }

        log.info("Successfully fetched and calculated {} exchange rates", rates.size());
        return rates;
    }

    private BigDecimal fetchCurrencyRate(String currencyPair, String fromDate, String toDate) {
        String seriesId = CURRENCY_SERIES.get(currencyPair);
        if (seriesId == null) {
            log.warn("No series ID found for currency pair: {}", currencyPair);
            return null;
        }

        String url = String.format(
                "%s%s/%s/%s/%s",
                riksbankBaseUrl,
                OBSERVATIONS_ENDPOINT,
                seriesId,
                fromDate,
                toDate
        );

        log.debug("Calling Riksbank API: {}", url);

        try {
            ResponseEntity<List<RiksbankObservation>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RiksbankObservation>>() {}
            );

            List<RiksbankObservation> observations = response.getBody();
            if (observations == null || observations.isEmpty()) {
                log.warn("No observations returned for {}", currencyPair);
                return null;
            }

            RiksbankObservation latest = observations.get(observations.size() - 1);

            if (latest.getValue() == null || latest.getValue().isEmpty()) {
                log.warn("Latest observation has no value for {}", currencyPair);
                return null;
            }

            BigDecimal rate = new BigDecimal(latest.getValue());
            log.debug("Fetched rate for {}: {} (date: {})", currencyPair, rate, latest.getDate());
            return rate;

        } catch (RestClientException e) {
            log.error("Failed to fetch rate for {} from Riksbank API", currencyPair, e);
            return null;
        } catch (NumberFormatException e) {
            log.error("Failed to parse rate value for {}", currencyPair, e);
            return null;
        }
    }

    public boolean isCurrencySupported(String currency) {
        if (currency == null || currency.isEmpty()) {
            return false;
        }
        return SUPPORTED_CURRENCIES.contains(currency.toUpperCase());
    }

    public List<String> getSupportedCurrencies() {
        return new ArrayList<>(SUPPORTED_CURRENCIES);
    }
}

