package com.moneytracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Secret must be >= 32 chars
        jwtService = new JwtService(
                "MySuperSecretKeyForJwtTesting1234567890",
                60000 // 1 minute
        );
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(1L, "testuser");

        assertNotNull(token, "Token should not be null");
        assertTrue(jwtService.validateToken(token), "Token should be valid");

        Long userId = jwtService.getUserIdFromToken(token);
        String username = jwtService.getUsernameFromToken(token);

        assertEquals(1L, userId, "UserId should match");
        assertEquals("testuser", username, "Username should match");
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertFalse(jwtService.validateToken(invalidToken), "Invalid token should be rejected");
    }

    @Test
    void testExpiredToken() throws InterruptedException {
        JwtService shortLivedService = new JwtService(
                "AnotherSuperSecretJwtKey12345678901234",
                1000 // 1 second
        );

        String token = shortLivedService.generateToken(2L, "tempuser");
        Thread.sleep(2000); // wait until token expires

        assertFalse(shortLivedService.validateToken(token), "Expired token should be invalid");
    }
}
