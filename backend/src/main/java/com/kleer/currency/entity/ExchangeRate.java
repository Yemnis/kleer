package com.kleer.currency.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing an exchange rate between two currencies.
 * 
 * Stores exchange rates with a unique constraint on currency pairs
 * to ensure only one rate exists per pair at any time.
 */
@Entity
@Table(
    name = "exchange_rate",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_currency_pair", 
        columnNames = {"from_currency", "to_currency"}
    ),
    indexes = {
        @Index(name = "idx_from_currency", columnList = "from_currency"),
        @Index(name = "idx_to_currency", columnList = "to_currency"),
        @Index(name = "idx_last_updated", columnList = "last_updated")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal rate;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}

