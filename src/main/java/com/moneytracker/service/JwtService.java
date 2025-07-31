package com.moneytracker.service;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String SECRET_KEY = "mySecretKey123456789"; // ⚡ Move to env variable

    public String generateToken(Long userId, String username) {
        return "jwt_" + userId + "_" + username + "_" + System.currentTimeMillis();
    }

    public Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("jwt_")) {
            throw new RuntimeException("Invalid token");
        }
        String[] parts = token.split("_");
        if (parts.length < 4) {
            throw new RuntimeException("Invalid token format");
        }
        return Long.parseLong(parts[1]);
    }

    public boolean validateToken(String token) {
        try {
            getUserIdFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
