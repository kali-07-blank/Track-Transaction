package com.moneytracker.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final Key key;
    private final long expirationTime;

    public JwtService(
            @Value("${jwt.secret:mySuperSecretKeyForJwtSigning1234567890}") String secret,
            @Value("${jwt.expiration:3600000}") long expirationTime // default 1 hour
    ) {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters for HS256.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
        logger.info("✅ JwtService initialized with expiration time {} ms", expirationTime);
    }

    /**
     * Generate a new JWT token for the given user.
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract userId from a JWT token.
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null || claims.getSubject() == null) {
            logger.error("❌ Invalid JWT: missing user ID");
            return null;
        }
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            logger.error("❌ Failed to parse userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract username from a JWT token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            logger.error("❌ Invalid JWT: missing claims");
            return null;
        }
        return claims.get("username", String.class);
    }

    /**
     * Validate a JWT token.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) {
                return false;
            }
            boolean isValid = claims.getExpiration().after(new Date());
            if (!isValid) {
                logger.warn("⚠️ JWT token is expired");
            }
            return isValid;
        } catch (ExpiredJwtException e) {
            logger.warn("⚠️ JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("❌ JWT token unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("❌ JWT token malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("❌ JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("❌ JWT token is null or empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract all claims from a JWT token.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("❌ Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract all custom claims as a Map.
     */
    public Map<String, Object> getAllCustomClaims(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims : Map.of();
    }
}
