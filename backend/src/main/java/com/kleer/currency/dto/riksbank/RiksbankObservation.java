package com.kleer.currency.dto.riksbank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for Riksbank API Observation response.
 *
 * Maps the JSON response from Riksbank's Observations endpoint.
 * Example: [{"date": "2025-11-03", "value": 10.935}]
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiksbankObservation {

    @JsonProperty("date")
    private String date;

    @JsonProperty("value")
    private String value;
}
