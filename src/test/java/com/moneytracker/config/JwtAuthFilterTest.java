package com.moneytracker.config;

import com.moneytracker.security.JwtRequestFilter;
import com.moneytracker.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private JwtRequestFilter jwtRequestFilter;
    private UserDetailsService userDetailsServiceMock;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // ✅ Use setters (we added them in JwtUtil)
        jwtUtil.setSecret("MySuperSecretJwtKeyForFilter12345678901234");
        jwtUtil.setExpiration(60L); // 60 seconds

        jwtRequestFilter = new JwtRequestFilter();
        userDetailsServiceMock = Mockito.mock(UserDetailsService.class);

        // Inject dependencies
        org.springframework.test.util.ReflectionTestUtils.setField(jwtRequestFilter, "jwtUtil", jwtUtil);
        org.springframework.test.util.ReflectionTestUtils.setField(jwtRequestFilter, "userDetailsService", userDetailsServiceMock);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testValidTokenAuthenticatesUser() throws ServletException, IOException {
        UserDetails user = User.withUsername("testuser")
                .password("password")
                .roles("USER")
                .build();

        when(userDetailsServiceMock.loadUserByUsername("testuser")).thenReturn(user);

        String token = jwtUtil.generateToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtRequestFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication should not be null for valid token");
        assertEquals("testuser",
                ((UsernamePasswordAuthenticationToken) SecurityContextHolder
                        .getContext().getAuthentication())
                        .getName());
    }

    @Test
    void testInvalidTokenLeavesContextUnauthenticated() throws ServletException, IOException {
        String invalidToken = "invalid.token.value";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtRequestFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication should be null for invalid token");
        assertEquals(200, response.getStatus() == 0 ? 200 : response.getStatus(),
                "Response should still be OK if invalid token is provided");
    }

    @Test
    void testNoTokenLeavesContextUnauthenticated() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtRequestFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Authentication should remain null when no token is provided");
        assertEquals(200, response.getStatus() == 0 ? 200 : response.getStatus(),
                "Response should be OK if no token provided");
    }
}
