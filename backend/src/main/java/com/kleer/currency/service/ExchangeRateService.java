package com.kleer.currency.service;

import com.kleer.currency.dto.ExchangeRateDto;
import com.kleer.currency.dto.ExchangeRatesResponse;
import com.kleer.currency.entity.ExchangeRate;
import com.kleer.currency.exception.ExchangeRateNotFoundException;
import com.kleer.currency.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing exchange rates in the database.
 * 
 * Handles CRUD operations for exchange rates and coordinates with RiksbankService
 * to fetch and store latest rates. Uses early returns throughout.
 */
@Service
@Slf4j
public class ExchangeRateService {

    private final ExchangeRateRepository repository;
    private final RiksbankService riksbankService;

    private static final List<String> SUPPORTED_CURRENCIES = List.of("SEK", "EUR", "USD");

    public ExchangeRateService(
            ExchangeRateRepository repository,
            RiksbankService riksbankService) {
        this.repository = repository;
        this.riksbankService = riksbankService;
    }

    /**
     * Refresh exchange rates from Riksbank API and save to database.
     * 
     * Fetches latest rates and generates all combinations including inverses.
     * Uses early returns for validation.
     *
     * @return Response containing all updated rates
     */
    @Transactional
    public ExchangeRatesResponse refreshRatesFromRiksbank() {
        log.info("Refreshing exchange rates from Riksbank");

        // Fetch rates from Riksbank
        Map<String, BigDecimal> riksbankRates = riksbankService.fetchLatestRates();

        // Early return if no rates fetched
        if (riksbankRates == null || riksbankRates.isEmpty()) {
            log.error("No rates received from Riksbank");
            throw new ExchangeRateNotFoundException("No rates available from Riksbank");
        }

        List<ExchangeRate> allRates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Store direct rates from Riksbank
        for (Map.Entry<String, BigDecimal> entry : riksbankRates.entrySet()) {
            String[] currencies = entry.getKey().split("/");
            
            // Early continue if invalid format
            if (currencies.length != 2) {
                continue;
            }

            String fromCurrency = currencies[0];
            String toCurrency = currencies[1];
            BigDecimal rate = entry.getValue();

            // Save direct rate
            ExchangeRate exchangeRate = saveOrUpdateRate(fromCurrency, toCurrency, rate, now);
            allRates.add(exchangeRate);

            // Calculate and save inverse rate
            BigDecimal inverseRate = BigDecimal.ONE.divide(rate, 8, RoundingMode.HALF_UP);
            ExchangeRate inverseExchangeRate = saveOrUpdateRate(toCurrency, fromCurrency, inverseRate, now);
            allRates.add(inverseExchangeRate);
        }

        // Generate cross rates (e.g., EUR to USD via SEK)
        generateCrossRates(now, allRates);

        log.info("Successfully refreshed {} exchange rates", allRates.size());

        return buildResponse(allRates);
    }

    /**
     * Get all exchange rates from database.
     *
     * @return Response containing all rates
     */
    @Transactional(readOnly = true)
    public ExchangeRatesResponse getAllRates() {
        log.debug("Fetching all exchange rates from database");

        List<ExchangeRate> rates = repository.findAll();

        // Early return if no rates in database
        if (rates.isEmpty()) {
            log.warn("No exchange rates found in database");
            return ExchangeRatesResponse.builder()
                    .rates(new ArrayList<>())
                    .lastUpdated(null)
                    .build();
        }

        return buildResponse(rates);
    }

    /**
     * Get exchange rate for a specific currency pair.
     * 
     * Uses early returns for validation and error handling.
     *
     * @param fromCurrency Source currency
     * @param toCurrency   Target currency
     * @return The exchange rate
     * @throws ExchangeRateNotFoundException if rate not found
     */
    @Transactional(readOnly = true)
    public ExchangeRate getRate(String fromCurrency, String toCurrency) {
        // Early return for null or empty currencies
        if (fromCurrency == null || fromCurrency.isEmpty()) {
            throw new IllegalArgumentException("Source currency cannot be null or empty");
        }

        if (toCurrency == null || toCurrency.isEmpty()) {
            throw new IllegalArgumentException("Target currency cannot be null or empty");
        }

        // Normalize to uppercase
        fromCurrency = fromCurrency.toUpperCase();
        toCurrency = toCurrency.toUpperCase();

        // Early return for unsupported currencies
        if (!SUPPORTED_CURRENCIES.contains(fromCurrency)) {
            throw new IllegalArgumentException("Unsupported source currency: " + fromCurrency);
        }

        if (!SUPPORTED_CURRENCIES.contains(toCurrency)) {
            throw new IllegalArgumentException("Unsupported target currency: " + toCurrency);
        }

        // Early return for same currency
        if (fromCurrency.equals(toCurrency)) {
            return ExchangeRate.builder()
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .rate(BigDecimal.ONE)
                    .lastUpdated(LocalDateTime.now())
                    .build();
        }

        Optional<ExchangeRate> rateOpt = repository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency);

        // Early return if rate not found
        if (rateOpt.isEmpty()) {
            log.error("Exchange rate not found: {} to {}", fromCurrency, toCurrency);
            throw new ExchangeRateNotFoundException(fromCurrency, toCurrency);
        }

        return rateOpt.get();
    }

    /**
     * Save or update an exchange rate.
     */
    private ExchangeRate saveOrUpdateRate(
            String fromCurrency, 
            String toCurrency, 
            BigDecimal rate, 
            LocalDateTime timestamp) {
        
        Optional<ExchangeRate> existingRate = 
                repository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency);

        ExchangeRate exchangeRate;
        
        if (existingRate.isPresent()) {
            // Update existing rate
            exchangeRate = existingRate.get();
            exchangeRate.setRate(rate);
            exchangeRate.setLastUpdated(timestamp);
            log.debug("Updating rate: {} to {} = {}", fromCurrency, toCurrency, rate);
        } else {
            // Create new rate
            exchangeRate = ExchangeRate.builder()
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .rate(rate)
                    .lastUpdated(timestamp)
                    .createdAt(timestamp)
                    .build();
            log.debug("Creating rate: {} to {} = {}", fromCurrency, toCurrency, rate);
        }

        return repository.save(exchangeRate);
    }

    /**
     * Generate cross rates for all currency combinations.
     * For example, EUR to USD via SEK.
     */
    private void generateCrossRates(LocalDateTime timestamp, List<ExchangeRate> allRates) {
        // For each pair of currencies, generate cross rate via SEK
        for (String currency1 : SUPPORTED_CURRENCIES) {
            for (String currency2 : SUPPORTED_CURRENCIES) {
                // Skip if same currency
                if (currency1.equals(currency2)) {
                    continue;
                }

                // Skip if already exists (direct rate from Riksbank)
                if (repository.existsByFromCurrencyAndToCurrency(currency1, currency2)) {
                    continue;
                }

                // Calculate cross rate via SEK
                try {
                    BigDecimal rate1ToSEK = getOrCalculateRateToSEK(currency1);
                    BigDecimal rate2ToSEK = getOrCalculateRateToSEK(currency2);
                    
                    // Early continue if either rate is null
                    if (rate1ToSEK == null || rate2ToSEK == null) {
                        continue;
                    }

                    BigDecimal crossRate = rate2ToSEK.divide(rate1ToSEK, 8, RoundingMode.HALF_UP);
                    ExchangeRate exchangeRate = saveOrUpdateRate(currency1, currency2, crossRate, timestamp);
                    allRates.add(exchangeRate);
                    
                } catch (Exception e) {
                    log.warn("Failed to generate cross rate for {} to {}: {}", 
                            currency1, currency2, e.getMessage());
                }
            }
        }
    }

    /**
     * Get or calculate exchange rate to SEK for a currency.
     */
    private BigDecimal getOrCalculateRateToSEK(String currency) {
        // Early return if already SEK
        if ("SEK".equals(currency)) {
            return BigDecimal.ONE;
        }

        Optional<ExchangeRate> rateOpt = repository.findByFromCurrencyAndToCurrency(currency, "SEK");
        
        return rateOpt.map(ExchangeRate::getRate).orElse(null);
    }

    /**
     * Build response from list of exchange rates.
     */
    private ExchangeRatesResponse buildResponse(List<ExchangeRate> rates) {
        List<ExchangeRateDto> rateDtos = rates.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        LocalDateTime latestUpdate = rates.stream()
                .map(ExchangeRate::getLastUpdated)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return ExchangeRatesResponse.builder()
                .rates(rateDtos)
                .lastUpdated(latestUpdate)
                .build();
    }

    /**
     * Convert entity to DTO.
     */
    private ExchangeRateDto toDto(ExchangeRate entity) {
        return ExchangeRateDto.builder()
                .fromCurrency(entity.getFromCurrency())
                .toCurrency(entity.getToCurrency())
                .rate(entity.getRate())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}

