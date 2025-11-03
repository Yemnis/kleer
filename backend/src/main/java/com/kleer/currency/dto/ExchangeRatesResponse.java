package com.kleer.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO containing all available exchange rates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRatesResponse {
    private List<ExchangeRateDto> rates;
    private LocalDateTime lastUpdated;
}

