package com.kleer.currency.controller;

import com.kleer.currency.dto.ConversionResponse;
import com.kleer.currency.service.CurrencyConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST Controller for currency conversion operations.
 * 
 * Provides endpoint to convert amounts between currencies.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ConversionController {

    private final CurrencyConversionService conversionService;

    public ConversionController(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Convert an amount from one currency to another.
     * 
     * GET /api/convert?amount={amount}&from={from}&to={to}
     *
     * @param amount Amount to convert
     * @param from   Source currency code (SEK, EUR, USD)
     * @param to     Target currency code (SEK, EUR, USD)
     * @return Conversion result with converted amount and rate
     */
    @GetMapping("/convert")
    public ResponseEntity<ConversionResponse> convertCurrency(
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        
        log.info("GET /api/convert - Converting {} {} to {}", amount, from, to);

        // Early return for null parameters (Spring handles this, but explicit check)
        if (amount == null) {
            log.warn("Amount parameter is null");
            throw new IllegalArgumentException("Amount is required");
        }

        if (from == null || from.trim().isEmpty()) {
            log.warn("From currency parameter is null or empty");
            throw new IllegalArgumentException("Source currency is required");
        }

        if (to == null || to.trim().isEmpty()) {
            log.warn("To currency parameter is null or empty");
            throw new IllegalArgumentException("Target currency is required");
        }

        ConversionResponse response = conversionService.convert(amount, from, to);

        log.info("Conversion successful: {} {} = {} {}", 
                amount, from, response.getConvertedAmount(), to);

        return ResponseEntity.ok(response);
    }
}

