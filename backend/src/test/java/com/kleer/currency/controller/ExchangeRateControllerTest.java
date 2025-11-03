package com.kleer.currency.controller;

import com.kleer.currency.dto.ExchangeRateDto;
import com.kleer.currency.dto.ExchangeRatesResponse;
import com.kleer.currency.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @Test
    void getLatestRates_shouldReturnRates() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        List<ExchangeRateDto> rates = new ArrayList<>();
        rates.add(ExchangeRateDto.builder()
                .fromCurrency("SEK")
                .toCurrency("EUR")
                .rate(new BigDecimal("0.0915"))
                .lastUpdated(now)
                .build());
        rates.add(ExchangeRateDto.builder()
                .fromCurrency("EUR")
                .toCurrency("SEK")
                .rate(new BigDecimal("10.9290"))
                .lastUpdated(now)
                .build());

        ExchangeRatesResponse response = ExchangeRatesResponse.builder()
                .rates(rates)
                .lastUpdated(now)
                .build();

        when(exchangeRateService.getAllRates()).thenReturn(response);

        mockMvc.perform(get("/api/rates/latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates[0].fromCurrency").value("SEK"))
                .andExpect(jsonPath("$.rates[0].toCurrency").value("EUR"))
                .andExpect(jsonPath("$.rates[0].rate").value(0.0915));

        verify(exchangeRateService).getAllRates();
    }

    @Test
    void refreshRates_shouldRefreshAndReturnRates() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        List<ExchangeRateDto> rates = new ArrayList<>();
        rates.add(ExchangeRateDto.builder()
                .fromCurrency("SEK")
                .toCurrency("EUR")
                .rate(new BigDecimal("0.0915"))
                .lastUpdated(now)
                .build());
        rates.add(ExchangeRateDto.builder()
                .fromCurrency("SEK")
                .toCurrency("USD")
                .rate(new BigDecimal("0.0962"))
                .lastUpdated(now)
                .build());

        ExchangeRatesResponse response = ExchangeRatesResponse.builder()
                .rates(rates)
                .lastUpdated(now)
                .build();

        when(exchangeRateService.refreshRatesFromRiksbank()).thenReturn(response);

        mockMvc.perform(post("/api/rates/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates[0].rate").value(0.0915))
                .andExpect(jsonPath("$.rates[1].rate").value(0.0962));

        verify(exchangeRateService).refreshRatesFromRiksbank();
    }

    @Test
    void getLatestRates_shouldReturnEmptyWhenNoRates() throws Exception {
        ExchangeRatesResponse response = ExchangeRatesResponse.builder()
                .rates(new ArrayList<>())
                .lastUpdated(LocalDateTime.now())
                .build();

        when(exchangeRateService.getAllRates()).thenReturn(response);

        mockMvc.perform(get("/api/rates/latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates").isEmpty());

        verify(exchangeRateService).getAllRates();
    }
}
