package com.kleer.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for exchange rate information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDto {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private LocalDateTime lastUpdated;
}

