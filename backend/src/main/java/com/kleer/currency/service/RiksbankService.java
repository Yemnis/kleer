package com.kleer.currency.service;

import com.kleer.currency.dto.riksbank.RiksbankObservation;
import com.kleer.currency.exception.RiksbankApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class RiksbankService {

    private final RestTemplate restTemplate;
    private final String riksbankBaseUrl;

    private static final List<String> SUPPORTED_CURRENCIES = List.of("SEK", "EUR", "USD");
    private static final String OBSERVATIONS_ENDPOINT = "/Observations";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Map<String, String> CURRENCY_SERIES = Map.of(
            "SEKEUR", "SEKEURPMI",
            "SEKUSD", "SEKUSDPMI"
    );

    public RiksbankService(
            RestTemplate restTemplate,
            @Value("${riksbank.api.base-url}") String riksbankBaseUrl) {
        this.restTemplate = restTemplate;
        this.riksbankBaseUrl = riksbankBaseUrl;
    }

    public Map<String, BigDecimal> fetchLatestRates() {
        log.info("Fetching latest exchange rates from Riksbank API");

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        String fromDate = weekAgo.format(DATE_FORMATTER);
        String toDate = today.format(DATE_FORMATTER);

        Map<String, BigDecimal> rates = new HashMap<>();

        BigDecimal sekToEur = fetchCurrencyRate("SEKEUR", fromDate, toDate);
        if (sekToEur != null) {
            rates.put("SEK/EUR", sekToEur);
            rates.put("EUR/SEK", BigDecimal.ONE.divide(sekToEur, 8, RoundingMode.HALF_UP));
        }

        BigDecimal sekToUsd = fetchCurrencyRate("SEKUSD", fromDate, toDate);
        if (sekToUsd != null) {
            rates.put("SEK/USD", sekToUsd);
            rates.put("USD/SEK", BigDecimal.ONE.divide(sekToUsd, 8, RoundingMode.HALF_UP));
        }

        if (sekToEur != null && sekToUsd != null) {
            BigDecimal eurToUsd = sekToUsd.divide(sekToEur, 8, RoundingMode.HALF_UP);
            rates.put("EUR/USD", eurToUsd);
            rates.put("USD/EUR", BigDecimal.ONE.divide(eurToUsd, 8, RoundingMode.HALF_UP));
        }

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

