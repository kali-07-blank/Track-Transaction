package com.moneytracker.util;

import com.moneytracker.entity.Person;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Utility class for JWT token operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
    private String secret;

    @Value("${jwt.access-token-expiration:3600000}") // 1 hour
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:86400000}") // 24 hours
    private Long refreshTokenExpiration;

    // In-memory token blacklist (use Redis in production)
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Generate access token for user
     */
    public String generateAccessToken(Person person) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", person.getId());
        claims.put("email", person.getEmail());
        claims.put("role", person.getRole().name());
        claims.put("type", "ACCESS");

        return createToken(claims, person.getUsername(), accessTokenExpiration);
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(Person person) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", person.getId());
        claims.put("type", "REFRESH");

        return createToken(claims, person.getUsername(), refreshTokenExpiration);
    }

    /**
     * Create JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract username from token (alias method for compatibility)
     */
    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token
     */
    public Boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token) && !isTokenBlacklisted(token);
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token with user details (alias method for compatibility)
     */
    public Boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) && !isTokenBlacklisted(token));
    }

    /**
     * Check if token is blacklisted
     */
    public Boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Invalidate token (add to blacklist)
     */
    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
    }

    /**
     * Get signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get access token expiration time
     */
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Get refresh token expiration time
     */
    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}