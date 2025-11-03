package com.kleer.currency.service;

import com.kleer.currency.dto.ConversionResponse;
import com.kleer.currency.entity.ExchangeRate;
import com.kleer.currency.exception.CurrencyNotSupportedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    private CurrencyConversionService currencyConversionService;

    @BeforeEach
    void setUp() {
        currencyConversionService = new CurrencyConversionService(exchangeRateService);
    }

    @Test
    void convert_shouldConvertSuccessfully() {
        BigDecimal amount = new BigDecimal("100");
        String fromCurrency = "SEK";
        String toCurrency = "EUR";
        BigDecimal rate = new BigDecimal("0.0915");

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFromCurrency(fromCurrency);
        exchangeRate.setToCurrency(toCurrency);
        exchangeRate.setRate(rate);

        when(exchangeRateService.getRate(fromCurrency, toCurrency)).thenReturn(exchangeRate);

        ConversionResponse response = currencyConversionService.convert(amount, fromCurrency, toCurrency);

        assertNotNull(response);
        assertEquals(amount, response.getOriginalAmount());
        assertEquals(new BigDecimal("9.15"), response.getConvertedAmount());
        assertEquals(rate, response.getRate());
        assertEquals(fromCurrency, response.getFromCurrency());
        assertEquals(toCurrency, response.getToCurrency());

        verify(exchangeRateService).getRate(fromCurrency, toCurrency);
    }

    @Test
    void convert_shouldReturnSameAmountForSameCurrency() {
        BigDecimal amount = new BigDecimal("100");
        String currency = "SEK";

        ConversionResponse response = currencyConversionService.convert(amount, currency, currency);

        assertNotNull(response);
        assertEquals(amount, response.getOriginalAmount());
        assertEquals(amount, response.getConvertedAmount());
        assertEquals(BigDecimal.ONE, response.getRate());
        assertEquals(currency, response.getFromCurrency());
        assertEquals(currency, response.getToCurrency());

        verify(exchangeRateService, never()).getRate(anyString(), anyString());
    }

    @Test
    void convert_shouldThrowExceptionForNullAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> currencyConversionService.convert(null, "SEK", "EUR"));
    }

    @Test
    void convert_shouldThrowExceptionForZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> currencyConversionService.convert(BigDecimal.ZERO, "SEK", "EUR"));
    }

    @Test
    void convert_shouldThrowExceptionForNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> currencyConversionService.convert(new BigDecimal("-10"), "SEK", "EUR"));
    }

    @Test
    void convert_shouldThrowExceptionForUnsupportedFromCurrency() {
        assertThrows(CurrencyNotSupportedException.class,
                () -> currencyConversionService.convert(new BigDecimal("100"), "GBP", "EUR"));
    }

    @Test
    void convert_shouldThrowExceptionForUnsupportedToCurrency() {
        assertThrows(CurrencyNotSupportedException.class,
                () -> currencyConversionService.convert(new BigDecimal("100"), "SEK", "JPY"));
    }
}
