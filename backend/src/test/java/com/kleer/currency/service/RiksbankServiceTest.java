package com.kleer.currency.service;

import com.kleer.currency.dto.riksbank.RiksbankObservation;
import com.kleer.currency.exception.RiksbankApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiksbankServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private RiksbankService riksbankService;

    private static final String RIKSBANK_BASE_URL = "https://api.riksbank.se/swea/v1";

    @BeforeEach
    void setUp() {
        riksbankService = new RiksbankService(restTemplate, RIKSBANK_BASE_URL);
    }

    @Test
    void fetchLatestRates_shouldReturnAllRatesWhenApiSuccess() {
        RiksbankObservation sekEurObs = new RiksbankObservation();
        sekEurObs.setDate("2025-11-03");
        sekEurObs.setValue("0.0915");

        RiksbankObservation sekUsdObs = new RiksbankObservation();
        sekUsdObs.setDate("2025-11-03");
        sekUsdObs.setValue("0.0962");

        when(restTemplate.exchange(
                contains("SEKEURPMI"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(List.of(sekEurObs), HttpStatus.OK));

        when(restTemplate.exchange(
                contains("SEKUSDPMI"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(List.of(sekUsdObs), HttpStatus.OK));

        Map<String, BigDecimal> rates = riksbankService.fetchLatestRates();

        assertNotNull(rates);
        assertEquals(6, rates.size());
        assertTrue(rates.containsKey("SEK/EUR"));
        assertTrue(rates.containsKey("EUR/SEK"));
        assertTrue(rates.containsKey("SEK/USD"));
        assertTrue(rates.containsKey("USD/SEK"));
        assertTrue(rates.containsKey("EUR/USD"));
        assertTrue(rates.containsKey("USD/EUR"));

        assertEquals(new BigDecimal("0.0915"), rates.get("SEK/EUR"));
        assertEquals(new BigDecimal("0.0962"), rates.get("SEK/USD"));
    }

    @Test
    void fetchLatestRates_shouldThrowExceptionWhenNoRatesAvailable() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("API Error"));

        assertThrows(RiksbankApiException.class, () -> riksbankService.fetchLatestRates());
    }

    @Test
    void isCurrencySupported_shouldReturnTrueForSupportedCurrencies() {
        assertTrue(riksbankService.isCurrencySupported("SEK"));
        assertTrue(riksbankService.isCurrencySupported("EUR"));
        assertTrue(riksbankService.isCurrencySupported("USD"));
        assertTrue(riksbankService.isCurrencySupported("sek"));
        assertTrue(riksbankService.isCurrencySupported("eur"));
    }

    @Test
    void isCurrencySupported_shouldReturnFalseForUnsupportedCurrencies() {
        assertFalse(riksbankService.isCurrencySupported("GBP"));
        assertFalse(riksbankService.isCurrencySupported("JPY"));
        assertFalse(riksbankService.isCurrencySupported(null));
        assertFalse(riksbankService.isCurrencySupported(""));
    }
}
