package com.moneytracker.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {
    // ❌ No CORS bean here, because SecurityConfig already defines it.
    // Keeping this class so package scanning works, but it's empty now.
}
