package com.kleer.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for currency conversion results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    private BigDecimal originalAmount;
    private BigDecimal convertedAmount;
    private BigDecimal rate;
    private String fromCurrency;
    private String toCurrency;
}

