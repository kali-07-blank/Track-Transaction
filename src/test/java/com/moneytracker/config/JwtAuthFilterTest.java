package com.moneytracker.config;

import com.moneytracker.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private SecurityConfig.JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "MySuperSecretJwtKeyForFilter12345678901234",
                60000 // 1 minute expiration
        );
        jwtAuthFilter = new SecurityConfig.JwtAuthFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testValidTokenAuthenticatesUser() throws ServletException, IOException {
        String token = jwtService.generateToken(1L, "testuser");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication should not be null for valid token");
        assertEquals("user-1",
                ((UsernamePasswordAuthenticationToken) SecurityContextHolder
                        .getContext().getAuthentication())
                        .getPrincipal().toString());
    }

    @Test
    void testInvalidTokenClearsContext() throws ServletException, IOException {
        String invalidToken = "invalid.token.value";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication should be null for invalid token");
        assertEquals(401, response.getStatus(),
                "Response status should be 401 Unauthorized");
    }

    @Test
    void testNoTokenLeavesContextUnauthenticated() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication should remain null when no token is provided");
        assertEquals(200, response.getStatus() == 0 ? 200 : response.getStatus(),
                "Response should be OK if no token provided");
    }
}
