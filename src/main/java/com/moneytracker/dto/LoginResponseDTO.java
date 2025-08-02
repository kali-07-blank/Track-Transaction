package com.moneytracker.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for login responses
 */
public class LoginResponseDTO {

    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private PersonDTO user;
    private LocalDateTime loginTime;

    // Constructors
    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, String refreshToken, Long expiresIn, PersonDTO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
        this.loginTime = LocalDateTime.now();
    }

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public PersonDTO getUser() { return user; }
    public void setUser(PersonDTO user) { this.user = user; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
}