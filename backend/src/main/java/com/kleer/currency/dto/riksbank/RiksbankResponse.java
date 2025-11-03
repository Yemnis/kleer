package com.kleer.currency.dto.riksbank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for Riksbank API response.
 * 
 * Maps the JSON response from Riksbank's Crossrates endpoint.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiksbankResponse {
    
    @JsonProperty("from")
    private String from;
    
    @JsonProperty("to")
    private List<String> to;
    
    @JsonProperty("groups")
    private List<CrossRateGroup> groups;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CrossRateGroup {
        
        @JsonProperty("groupid")
        private String groupId;
        
        @JsonProperty("series")
        private List<CrossRateSeries> series;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CrossRateSeries {
        
        @JsonProperty("seriesid")
        private String seriesId;
        
        @JsonProperty("values")
        private List<CrossRateValue> values;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CrossRateValue {
        
        @JsonProperty("date")
        private String date;
        
        @JsonProperty("value")
        private String value;
    }
}

