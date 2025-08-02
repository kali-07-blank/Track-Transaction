// MoneyTrackerApplication.java
package com.moneytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.moneytracker.repository")
@ComponentScan(basePackages = "com.moneytracker")
public class MoneyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoneyTrackerApplication.class, args);
    }
}
