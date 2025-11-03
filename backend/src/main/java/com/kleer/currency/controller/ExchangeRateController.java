package com.kleer.currency.controller;

import com.kleer.currency.dto.ExchangeRatesResponse;
import com.kleer.currency.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for exchange rate operations.
 * 
 * Provides endpoints to fetch and refresh exchange rates.
 */
@RestController
@RequestMapping("/api/rates")
@Slf4j
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * Get all available exchange rates from the database.
     * 
     * GET /api/rates/latest
     *
     * @return Response containing all exchange rates
     */
    @GetMapping("/latest")
    public ResponseEntity<ExchangeRatesResponse> getLatestRates() {
        log.info("GET /api/rates/latest - Fetching latest exchange rates");

        ExchangeRatesResponse response = exchangeRateService.getAllRates();

        // Early return if no rates available
        if (response.getRates() == null || response.getRates().isEmpty()) {
            log.warn("No exchange rates available in database");
            return ResponseEntity.ok(response);
        }

        log.info("Returning {} exchange rates", response.getRates().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Trigger refresh of exchange rates from Riksbank API.
     * 
     * POST /api/rates/refresh
     *
     * @return Response containing updated exchange rates
     */
    @PostMapping("/refresh")
    public ResponseEntity<ExchangeRatesResponse> refreshRates() {
        log.info("POST /api/rates/refresh - Refreshing exchange rates from Riksbank");

        ExchangeRatesResponse response = exchangeRateService.refreshRatesFromRiksbank();

        log.info("Successfully refreshed {} exchange rates", response.getRates().size());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

