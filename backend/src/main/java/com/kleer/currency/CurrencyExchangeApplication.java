package com.kleer.currency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Currency Exchange Service.
 * 
 * This application provides currency exchange functionality with rates
 * fetched from Riksbank's open API and stored in an H2 database.
 */
@SpringBootApplication
public class CurrencyExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyExchangeApplication.class, args);
    }
}

