package com.moneytracker.service;

import com.moneytracker.dto.LoginRequestDTO;
import com.moneytracker.dto.LoginResponseDTO;
import com.moneytracker.dto.PersonDTO;
import com.moneytracker.dto.RegisterRequestDTO;

/**
 * Service interface for authentication operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
public interface AuthService {

    /**
     * Register a new user
     * @param registerRequest registration data
     * @return person DTO
     */
    PersonDTO register(RegisterRequestDTO registerRequest);

    /**
     * Authenticate user and generate JWT token
     * @param loginRequest login credentials
     * @return login response with JWT token
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);

    /**
     * Refresh JWT token
     * @param refreshToken refresh token
     * @return new login response with refreshed token
     */
    LoginResponseDTO refreshToken(String refreshToken);

    /**
     * Logout user (invalidate tokens)
     * @param token JWT token to invalidate
     */
    void logout(String token);
}