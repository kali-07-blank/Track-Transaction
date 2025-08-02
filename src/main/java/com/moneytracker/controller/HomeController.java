package com.moneytracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "✅ Money Tracker API is running! Use /api/auth/login or /api/auth/register";
    }
}
