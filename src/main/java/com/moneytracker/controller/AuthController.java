package com.moneytracker.controller;

import com.moneytracker.dto.*;
import com.moneytracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<PersonDTO>> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        logger.info("Registration request received for username: {}", registerRequest.getUsername());

        PersonDTO person = authService.register(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(person, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        logger.info("Login request received for identifier: {}", loginRequest.getIdentifier());

        LoginResponseDTO loginResponse = authService.login(loginRequest);

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refreshToken(@RequestBody RefreshTokenRequestDTO refreshRequest) {
        logger.info("Token refresh request received");

        LoginResponseDTO loginResponse = authService.refreshToken(refreshRequest.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        logger.info("Logout request received");

        String token = extractTokenFromRequest(request);
        if (token != null) {
            authService.logout(token);
        }

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}