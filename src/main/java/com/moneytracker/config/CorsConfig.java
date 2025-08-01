package com.moneytracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // Explicit origins (no "*")
                        .allowedOriginPatterns(
                                "http://localhost:8080",
                                "http://127.0.0.1:8080",
                                "https://cheerful-tiramisu-44b0ee.netlify.app",
                                "https://track-transaction.onrender.com"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization") // expose JWT in headers if needed
                        .allowCredentials(true)
                        .maxAge(3600); // cache preflight response for 1 hour
            }
        };
    }
}
