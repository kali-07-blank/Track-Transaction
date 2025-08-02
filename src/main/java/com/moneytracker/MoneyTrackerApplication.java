package com.moneytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for Money Tracker.
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class MoneyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoneyTrackerApplication.class, args);
    }
}