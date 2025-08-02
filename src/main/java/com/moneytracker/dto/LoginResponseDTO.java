package com.moneytracker.dto;

public class LoginResponseDTO {
    private boolean success;
    private String message;
    private String token;
    private String username;
    private PersonDTO person;

    public LoginResponseDTO() {}

    public LoginResponseDTO(boolean success, String message, String token, String username, PersonDTO person) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.username = username;
        this.person = person;
    }

    public LoginResponseDTO(boolean success, String message, String token, String username) {
        this(success, message, token, username, null);
    }

    // Getters & Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public PersonDTO getPerson() { return person; }
    public void setPerson(PersonDTO person) { this.person = person; }
}
