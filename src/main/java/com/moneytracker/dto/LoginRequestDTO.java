// LoginRequestDTO.java
package com.moneytracker.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {
    @NotBlank(message = "Identifier (username or email) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequestDTO() {}

    public LoginRequestDTO(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
