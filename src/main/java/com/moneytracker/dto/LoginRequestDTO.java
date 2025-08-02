package com.moneytracker.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for login requests
 */
public class LoginRequestDTO {

    @NotBlank(message = "Identifier (username or email) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    // Constructors
    public LoginRequestDTO() {}

    public LoginRequestDTO(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    // Getters and setters
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}