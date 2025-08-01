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
import java.util.Optional;

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
        logger.info("JwtService initialized with expiration time {} ms", expirationTime);
    }

    /**
     * Generate a new JWT token for the given user.
     *
     * @param userId   the ID of the user
     * @param username the username of the user
     * @return signed JWT token as String
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
     *
     * @param token the JWT token
     * @return Optional containing userId if present
     */
    public Optional<Long> getUserIdFromToken(String token) {
        return Optional.ofNullable(extractAllClaims(token))
                .map(claims -> Long.parseLong(claims.getSubject()));
    }

    /**
     * Extract username from a JWT token.
     *
     * @param token the JWT token
     * @return Optional containing username if present
     */
    public Optional<String> getUsernameFromToken(String token) {
        return Optional.ofNullable(extractAllClaims(token))
                .map(claims -> claims.get("username", String.class));
    }

    /**
     * Validate a JWT token.
     *
     * @param token the JWT token
     * @return true if token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            boolean isValid = claims.getExpiration().after(new Date());
            if (!isValid) {
                logger.warn("JWT token is expired");
            }
            return isValid;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("JWT token malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT token is null or empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract all claims from a JWT token.
     *
     * @param token the JWT token
     * @return Claims object if parsing succeeds, otherwise null
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract all custom claims as a Map.
     *
     * @param token the JWT token
     * @return Optional containing Map of claims
     */
    public Optional<Map<String, Object>> getAllCustomClaims(String token) {
        return Optional.ofNullable(extractAllClaims(token))
                .map(claims -> claims);
    }
}
